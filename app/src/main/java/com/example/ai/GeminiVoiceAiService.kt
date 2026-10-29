package com.example.ai

import android.util.Log
import com.example.audio.VocalDnaMetrics
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.File

private const val TAG = "GeminiVoiceAiService"

data class AiVoiceCloneProfile(
    val suggestedNameBn: String,
    val suggestedNameEn: String,
    val gender: String,
    val languageTag: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val sampleTranscript: String,
    val recommendedEmotionKey: String
)

data class AiAudioTranscriptionResult(
    val transcriptText: String,
    val detectedEmotionKey: String,
    val speakerSummaryBn: String,
    val speakerSummaryEn: String
)

class GeminiVoiceAiService {

    private val voiceCloneSchema = Schema.obj(
        properties = mapOf(
            "suggestedNameBn" to Schema.string(description = "A realistic descriptive Bengali name for this voice profile"),
            "suggestedNameEn" to Schema.string(description = "A realistic descriptive English name for this voice profile"),
            "gender" to Schema.enumeration(
                values = listOf("MALE", "FEMALE", "NEUTRAL"),
                description = "Detected speaker vocal gender"
            ),
            "languageTag" to Schema.enumeration(
                values = listOf("bn-BD", "en-US"),
                description = "Primary language detected in the voice sample"
            ),
            "descriptionBn" to Schema.string(description = "Detailed Bengali description of the speaker's tone, timbre, pitch, and vocal warmth"),
            "descriptionEn" to Schema.string(description = "Detailed English description of the speaker's tone, timbre, pitch, and vocal warmth"),
            "sampleTranscript" to Schema.string(description = "Exact transcription of what was spoken in the audio sample"),
            "recommendedEmotionKey" to Schema.enumeration(
                values = listOf(
                    "NATURAL", "CHEERFUL", "DEEP_SERIOUS", "NEWS_ANCHOR",
                    "STORYTELLER", "EMOTIONAL", "WHISPER", "ENERGETIC_YT"
                ),
                description = "Best matching emotion style key"
            )
        )
    )

    private val transcriptionSchema = Schema.obj(
        properties = mapOf(
            "transcriptText" to Schema.string(description = "Exact verbatim transcription of the spoken words in Bengali or English"),
            "detectedEmotionKey" to Schema.enumeration(
                values = listOf(
                    "NATURAL", "CHEERFUL", "DEEP_SERIOUS", "NEWS_ANCHOR",
                    "STORYTELLER", "EMOTIONAL", "WHISPER", "ENERGETIC_YT"
                ),
                description = "Detected emotion in the audio"
            ),
            "speakerSummaryBn" to Schema.string(description = "Short Bengali summary of the vocal delivery"),
            "speakerSummaryEn" to Schema.string(description = "Short English summary of the vocal delivery")
        )
    )

    private val voiceCloneModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = voiceCloneSchema
            }
        )
    }

    private val transcriptionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = transcriptionSchema
            }
        )
    }

    private val scriptWriterModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
    }

    /**
     * Multimodal Voice Cloning Analysis:
     * Sends the uploaded/recorded audio bytes (up to 15MB) + DSP acoustic metrics to Gemini 3.8 Flash
     * to extract speaker vocal characteristics, tone, cadence, and exact transcription.
     */
    suspend fun analyzeAudioForVoiceCloning(
        audioFile: File,
        dspMetrics: VocalDnaMetrics,
        customVoiceName: String,
        mimeType: String = "audio/wav"
    ): Result<AiVoiceCloneProfile> = withContext(Dispatchers.IO) {
        try {
            val audioBytes = if (audioFile.exists() && audioFile.length() in 1..15_000_000L) {
                audioFile.readBytes()
            } else {
                ByteArray(0)
            }

            val promptText = """
                Analyze this human voice sample for high-precision acoustic voice cloning.
                Measured DSP Acoustic Telemetry:
                - Fundamental Frequency (F0): ${dspMetrics.fundamentalHz} Hz
                - Base Pitch Ratio: ${dspMetrics.basePitchRatio}x
                - Formant Resonance Shift: ${dspMetrics.formantShift}x
                - Warmth / Bass Shelf: +${dspMetrics.warmthDb} dB
                - Studio Clarity Shelf: +${dspMetrics.clarityDb} dB
                - User-provided name hint: "$customVoiceName"
                
                Listen carefully to the voice timbre, accent, speaking cadence, and emotion.
                Provide a rich Bengali and English vocal profile description and transcribe what the speaker said.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                if (audioBytes.isNotEmpty()) {
                    voiceCloneModel.generateContent(
                        content {
                            inlineData(audioBytes, mimeType)
                            text(promptText)
                        }
                    )
                } else {
                    voiceCloneModel.generateContent(promptText)
                }
            }

            val rawJson = response.text ?: throw IllegalStateException("Empty AI response")
            val json = JSONObject(rawJson)

            Result.success(
                AiVoiceCloneProfile(
                    suggestedNameBn = json.optString("suggestedNameBn").ifBlank {
                        customVoiceName.ifBlank { "ক্লোন করা কণ্ঠ (${dspMetrics.fundamentalHz}Hz)" }
                    },
                    suggestedNameEn = json.optString("suggestedNameEn").ifBlank {
                        customVoiceName.ifBlank { "Cloned Voice (${dspMetrics.fundamentalHz}Hz)" }
                    },
                    gender = json.optString("gender", dspMetrics.detectedGender),
                    languageTag = json.optString("languageTag", "bn-BD"),
                    descriptionBn = json.optString("descriptionBn"),
                    descriptionEn = json.optString("descriptionEn"),
                    sampleTranscript = json.optString("sampleTranscript"),
                    recommendedEmotionKey = json.optString("recommendedEmotionKey", "NATURAL")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Voice cloning AI analysis timed out", e)
            Result.failure(Exception("ভয়েস এআই বিশ্লেষণ সময়সীমা অতিক্রম করেছে (Timed out after 60s)."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Voice cloning AI analysis failed", e)
            Result.failure(e)
        }
    }

    /**
     * Transcribes an uploaded or recorded audio file for Voice-to-Voice conversion.
     */
    suspend fun transcribeAudioForVoiceChanger(
        audioFile: File,
        mimeType: String = "audio/wav"
    ): Result<AiAudioTranscriptionResult> = withContext(Dispatchers.IO) {
        try {
            val audioBytes = if (audioFile.exists() && audioFile.length() in 1..15_000_000L) {
                audioFile.readBytes()
            } else {
                throw IllegalArgumentException("Audio file is empty or exceeds 15MB limit")
            }

            val response = withTimeout(60_000L) {
                transcriptionModel.generateContent(
                    content {
                        inlineData(audioBytes, mimeType)
                        text(
                            "Listen to this audio recording and transcribe the exact spoken words in Bengali or English. " +
                                "Also identify the speaker's emotional tone."
                        )
                    }
                )
            }

            val rawJson = response.text ?: throw IllegalStateException("Empty transcription response")
            val json = JSONObject(rawJson)
            Result.success(
                AiAudioTranscriptionResult(
                    transcriptText = json.getString("transcriptText"),
                    detectedEmotionKey = json.optString("detectedEmotionKey", "NATURAL"),
                    speakerSummaryBn = json.optString("speakerSummaryBn", "অডিও ট্রান্সক্রিপশন সম্পন্ন"),
                    speakerSummaryEn = json.optString("speakerSummaryEn", "Audio transcription completed")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Audio transcription timed out", e)
            Result.failure(Exception("অডিও ট্রান্সক্রিপশন সময়সীমা অতিক্রম করেছে (Timed out after 60s)."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Audio transcription failed", e)
            Result.failure(e)
        }
    }

    /**
     * Generates or polishes a voiceover script in Bengali or English using Gemini 3.8 Flash.
     */
    suspend fun generateOrPolishVoiceScript(
        topicOrDraft: String,
        styleCategory: String,
        isBengali: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val langInstruction = if (isBengali) {
                "Write the voiceover script in natural, expressive, spoken Bangladeshi/Standard Bengali (বাংলা)."
            } else {
                "Write the voiceover script in natural, expressive, broadcast-ready English."
            }

            val prompt = """
                You are an expert voiceover scriptwriter.
                Style/Genre: $styleCategory
                Topic or User Draft: "$topicOrDraft"
                $langInstruction
                
                Rules:
                - Output ONLY the spoken script text itself (no stage directions, no [brackets], no "Narrator:" labels, no markdown headers).
                - Use natural punctuation (commas, periods, exclamation marks) so a Text-to-Speech engine pauses naturally like a real human speaker.
                - Keep it engaging, clear, and between 35 to 85 words.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                scriptWriterModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Model returned an empty script."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Script generation timed out", e)
            Result.failure(Exception("স্ক্রিপ্ট তৈরিতে সময় বেশি লাগছে, আবার চেষ্টা করুন (Timed out after 60s)."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Script generation failed", e)
            Result.failure(e)
        }
    }
}
