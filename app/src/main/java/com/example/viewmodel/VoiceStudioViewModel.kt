package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiVoiceAiService
import com.example.audio.AcousticVoiceDsp
import com.example.audio.AudioStudioController
import com.example.audio.PlaybackState
import com.example.audio.VocalDnaMetrics
import com.example.data.ClonedVoiceEntity
import com.example.data.GeneratedAudioEntity
import com.example.data.VoiceEmotion
import com.example.data.VoiceStudioDatabase
import com.example.data.VoiceStudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val TAG = "VoiceStudioViewModel"

enum class StudioTab {
    TTS_STUDIO,
    VOICE_CLONE,
    VOICE_CHANGER,
    AUDIO_LIBRARY
}

enum class VoiceChangerMode {
    AI_SMART_RESYNTHESIS, // Transcribe + synthesize in cloned/target voice with full articulation
    DIRECT_ACOUSTIC_MORPH // Direct F0 pitch & formant reshaping of the original audio waveform
}

data class CloneDraftState(
    val sourceFilePath: String? = null,
    val sourceFileName: String = "",
    val sourceMimeType: String = "audio/wav",
    val isRecording: Boolean = false,
    val isAnalyzing: Boolean = false,
    val customVoiceName: String = "",
    val extractedDna: VocalDnaMetrics? = null,
    val aiDescriptionBn: String = "",
    val aiDescriptionEn: String = "",
    val sampleTranscript: String = "",
    val previewFilePath: String? = null,
    val statusMessage: String? = null,
    val isError: Boolean = false
)

data class VoiceChangerDraftState(
    val inputFilePath: String? = null,
    val inputFileName: String = "",
    val inputMimeType: String = "audio/wav",
    val isRecording: Boolean = false,
    val isConverting: Boolean = false,
    val mode: VoiceChangerMode = VoiceChangerMode.DIRECT_ACOUSTIC_MORPH,
    val transcribedText: String = "",
    val outputFilePath: String? = null,
    val outputWaveformPeaks: List<Float> = List(48) { 0.3f },
    val statusMessage: String? = null,
    val isError: Boolean = false
)

data class StudioUiState(
    val isBengali: Boolean = true,
    val currentTab: StudioTab = StudioTab.TTS_STUDIO,
    val scriptText: String = "স্বাগতম কণ্ঠ স্টুডিওতে! এখানে আপনি যেকোনো লেখা থেকে মানুষের মতো স্বাভাবিক ভয়েস তৈরি করতে পারবেন এবং যেকোনো কণ্ঠস্বর হুবহু ক্লোন করতে পারবেন।",
    val selectedVoiceId: Long = 1L,
    val selectedEmotion: VoiceEmotion = VoiceEmotion.NATURAL,
    val userPitchMultiplier: Float = 1.0f,
    val userSpeedMultiplier: Float = 1.0f,
    val userWarmthOffsetDb: Float = 0.0f,
    val userClarityOffsetDb: Float = 0.0f,
    val isSynthesizing: Boolean = false,
    val isGeneratingScript: Boolean = false,
    val latestGeneratedAudioPath: String? = null,
    val latestGeneratedTitle: String = "",
    val latestWaveformPeaks: List<Float> = List(48) { 0.32f },
    val bannerMessage: String? = null,
    val isBannerError: Boolean = false,
    val cloneDraft: CloneDraftState = CloneDraftState(),
    val changerDraft: VoiceChangerDraftState = VoiceChangerDraftState()
)

class VoiceStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VoiceStudioDatabase.getInstance(application)
    private val repository = VoiceStudioRepository(database.voiceStudioDao())
    private val aiService = GeminiVoiceAiService()
    val audioController = AudioStudioController(application, viewModelScope)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val voices: StateFlow<List<ClonedVoiceEntity>> = repository.allVoices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = VoiceStudioRepository.defaultHumanVoices()
        )

    val generatedAudios: StateFlow<List<GeneratedAudioEntity>> = repository.generatedAudios
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val playbackState: StateFlow<PlaybackState> = audioController.playbackState
    val recordingAmplitude: StateFlow<Float> = audioController.recordingAmplitude
    val recordingSeconds: StateFlow<Int> = audioController.recordingSeconds

    private var activeRecordingFile: File? = null

    init {
        viewModelScope.launch {
            repository.ensureDefaultVoicesSeeded()
        }
    }

    fun toggleLanguage() {
        _uiState.update { state ->
            val nextBengali = !state.isBengali
            val defaultBn = "স্বাগতম কণ্ঠ স্টুডিওতে! এখানে আপনি যেকোনো লেখা থেকে মানুষের মতো স্বাভাবিক ভয়েস তৈরি করতে পারবেন এবং যেকোনো কণ্ঠস্বর হুবহু ক্লোন করতে পারবেন।"
            val defaultEn = "Welcome to KobiVoice Studio! Transform any text into ultra-realistic human speech or clone any voice from an MP3 or microphone recording."
            val updatedScript = if (state.scriptText == defaultBn && !nextBengali) {
                defaultEn
            } else if (state.scriptText == defaultEn && nextBengali) {
                defaultBn
            } else {
                state.scriptText
            }
            state.copy(isBengali = nextBengali, scriptText = updatedScript)
        }
    }

    fun selectTab(tab: StudioTab) {
        _uiState.update { it.copy(currentTab = tab, bannerMessage = null) }
    }

    fun updateScriptText(text: String) {
        _uiState.update { it.copy(scriptText = text) }
    }

    fun selectVoice(voiceId: Long) {
        _uiState.update { it.copy(selectedVoiceId = voiceId) }
    }

    fun selectEmotion(emotion: VoiceEmotion) {
        _uiState.update { it.copy(selectedEmotion = emotion) }
    }

    fun updatePitchMultiplier(pitch: Float) {
        _uiState.update { it.copy(userPitchMultiplier = pitch.coerceIn(0.65f, 1.45f)) }
    }

    fun updateSpeedMultiplier(speed: Float) {
        _uiState.update { it.copy(userSpeedMultiplier = speed.coerceIn(0.65f, 1.45f)) }
    }

    fun updateWarmthOffset(warmthDb: Float) {
        _uiState.update { it.copy(userWarmthOffsetDb = warmthDb.coerceIn(-5f, 8f)) }
    }

    fun updateClarityOffset(clarityDb: Float) {
        _uiState.update { it.copy(userClarityOffsetDb = clarityDb.coerceIn(-4f, 8f)) }
    }

    fun resetVoiceSliders() {
        _uiState.update {
            it.copy(
                userPitchMultiplier = 1.0f,
                userSpeedMultiplier = 1.0f,
                userWarmthOffsetDb = 0.0f,
                userClarityOffsetDb = 0.0f,
                selectedEmotion = VoiceEmotion.NATURAL
            )
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(bannerMessage = null, isBannerError = false) }
    }

    fun getSelectedVoice(): ClonedVoiceEntity {
        val currentList = voices.value
        return currentList.find { it.id == _uiState.value.selectedVoiceId }
            ?: currentList.firstOrNull()
            ?: VoiceStudioRepository.defaultHumanVoices().first()
    }

    /**
     * AI Script Writer & Polisher using Gemini 3.8 Flash
     */
    fun generateAiScript(topic: String, category: String) {
        val state = _uiState.value
        val effectiveTopic = topic.ifBlank {
            state.scriptText.ifBlank {
                if (state.isBengali) "একটি অনুপ্রেরণামূলক সফলতার গল্প" else "An inspiring story about chasing dreams"
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingScript = true, bannerMessage = null) }
            val result = aiService.generateOrPolishVoiceScript(
                topicOrDraft = effectiveTopic,
                styleCategory = category,
                isBengali = state.isBengali
            )
            result.onSuccess { generatedText ->
                _uiState.update {
                    it.copy(
                        isGeneratingScript = false,
                        scriptText = generatedText,
                        bannerMessage = if (it.isBengali) "✨ এআই স্ক্রিপ্ট তৈরি হয়েছে!" else "✨ AI Script generated!",
                        isBannerError = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isGeneratingScript = false,
                        bannerMessage = err.localizedMessage ?: "AI Script generation failed",
                        isBannerError = true
                    )
                }
            }
        }
    }

    /**
     * Synthesizes the current script into a high-definition mastered WAV file using the selected
     * human or cloned voice profile + acoustic DNA parameters.
     */
    fun synthesizeCurrentScript() {
        val state = _uiState.value
        val text = state.scriptText.trim()
        if (text.isBlank()) {
            _uiState.update {
                it.copy(
                    bannerMessage = if (it.isBengali) "অনুগ্রহ করে প্রথমে কিছু টেক্সট লিখুন।" else "Please enter some text first.",
                    isBannerError = true
                )
            }
            return
        }

        val voice = getSelectedVoice()
        viewModelScope.launch {
            _uiState.update { it.copy(isSynthesizing = true, bannerMessage = null) }
            val result = audioController.synthesizeSpeechToMasteredWav(
                text = text,
                voiceProfile = voice,
                emotion = state.selectedEmotion,
                userPitchOffset = state.userPitchMultiplier,
                userSpeedOffset = state.userSpeedMultiplier,
                userWarmthOffsetDb = state.userWarmthOffsetDb,
                userClarityOffsetDb = state.userClarityOffsetDb
            )

            result.onSuccess { (wavFile, peaks) ->
                val shortTitle = text.take(36).replace("\n", " ")
                val decodedDur = AcousticVoiceDsp.extractVocalDnaFromFile(wavFile).durationMs
                val entity = GeneratedAudioEntity(
                    title = shortTitle,
                    scriptText = text,
                    voiceNameBn = voice.nameBn,
                    voiceNameEn = voice.nameEn,
                    emotionKey = state.selectedEmotion.key,
                    audioFilePath = wavFile.absolutePath,
                    durationMs = decodedDur,
                    isVoiceChanged = false
                )
                repository.saveGeneratedAudio(entity)

                _uiState.update {
                    it.copy(
                        isSynthesizing = false,
                        latestGeneratedAudioPath = wavFile.absolutePath,
                        latestGeneratedTitle = shortTitle,
                        latestWaveformPeaks = peaks,
                        bannerMessage = if (it.isBengali) {
                            "✅ '${voice.nameBn}' কণ্ঠে ভয়েস তৈরি সম্পন্ন!"
                        } else {
                            "✅ Voice generated with '${voice.nameEn}'!"
                        },
                        isBannerError = false
                    )
                }
                audioController.playAudioFile(wavFile.absolutePath, shortTitle, peaks)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSynthesizing = false,
                        bannerMessage = err.localizedMessage ?: "Voice generation failed",
                        isBannerError = true
                    )
                }
            }
        }
    }

    // =========================================================================
    // VOICE CLONING STUDIO FLOWS (Live Microphone & MP3/Audio File Upload)
    // =========================================================================

    fun updateCloneVoiceName(name: String) {
        _uiState.update {
            it.copy(cloneDraft = it.cloneDraft.copy(customVoiceName = name))
        }
    }

    fun startVoiceCloneRecording() {
        audioController.stopPlayback()
        val file = audioController.startMicrophoneRecording()
        if (file != null) {
            activeRecordingFile = file
            _uiState.update {
                it.copy(
                    cloneDraft = it.cloneDraft.copy(
                        isRecording = true,
                        statusMessage = if (it.isBengali) "🎙️ আপনার কণ্ঠ রেকর্ড হচ্ছে... স্ক্রিপ্টটি স্পষ্টভাবে পড়ুন" else "🎙️ Recording your voice... Read the script clearly",
                        isError = false
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    cloneDraft = it.cloneDraft.copy(
                        isRecording = false,
                        statusMessage = if (it.isBengali) "মাইক্রোফোন চালু করা যায়নি। পারমিশন চেক করুন।" else "Could not start microphone.",
                        isError = true
                    )
                )
            }
        }
    }

    fun stopVoiceCloneRecording() {
        viewModelScope.launch {
            audioController.stopMicrophoneRecording()
            val file = activeRecordingFile
            if (file != null && file.exists()) {
                val dna = withContext(Dispatchers.IO) {
                    AcousticVoiceDsp.extractVocalDnaFromFile(file)
                }
                _uiState.update {
                    it.copy(
                        cloneDraft = it.cloneDraft.copy(
                            isRecording = false,
                            sourceFilePath = file.absolutePath,
                            sourceFileName = file.name,
                            sourceMimeType = "audio/wav",
                            extractedDna = dna,
                            statusMessage = if (it.isBengali) {
                                "✅ কণ্ঠ রেকর্ড সম্পন্ন (${dna.fundamentalHz} Hz পিচ শনাক্ত)। এখন 'হুবহু ভয়েস ক্লোন করুন' বাটনে ট্যাপ করুন।"
                            } else {
                                "✅ Voice recorded (${dna.fundamentalHz} Hz F0 detected). Tap 'Clone Voice Now'."
                            },
                            isError = false
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(cloneDraft = it.cloneDraft.copy(isRecording = false))
                }
            }
        }
    }

    fun onAudioFilePickedForCloning(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    cloneDraft = it.cloneDraft.copy(
                        isAnalyzing = true,
                        statusMessage = if (it.isBengali) "📂 অডিও ফাইল ইমপোর্ট ও কণ্ঠের ডিএনএ বিশ্লেষণ হচ্ছে..." else "📂 Importing audio file & extracting Vocal DNA...",
                        isError = false
                    )
                )
            }
            val copiedResult = copyUriToInternalStorage(uri, "voice_samples")
            copiedResult.onSuccess { (file, displayName, mimeType) ->
                val dna = withContext(Dispatchers.IO) {
                    AcousticVoiceDsp.extractVocalDnaFromFile(file)
                }
                val suggestedDefaultName = displayName.substringBeforeLast(".")
                    .replace("_", " ")
                    .take(24)

                _uiState.update { state ->
                    state.copy(
                        cloneDraft = state.cloneDraft.copy(
                            isAnalyzing = false,
                            sourceFilePath = file.absolutePath,
                            sourceFileName = displayName,
                            sourceMimeType = mimeType,
                            extractedDna = dna,
                            customVoiceName = state.cloneDraft.customVoiceName.ifBlank { suggestedDefaultName },
                            statusMessage = if (state.isBengali) {
                                "✅ '$displayName' থেকে কণ্ঠের তরঙ্গ (${dna.fundamentalHz} Hz) পাওয়া গেছে! এখন ক্লোন সম্পন্ন করুন।"
                            } else {
                                "✅ Extracted vocal profile (${dna.fundamentalHz} Hz) from '$displayName'!"
                            },
                            isError = false
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update { state ->
                    state.copy(
                        cloneDraft = state.cloneDraft.copy(
                            isAnalyzing = false,
                            statusMessage = err.localizedMessage ?: "Failed to read audio file",
                            isError = true
                        )
                    )
                }
            }
        }
    }

    /**
     * Executes full Dual-Layer Voice Cloning:
     * 1. Extracts exact F0 pitch, formant shift, warmth & clarity EQ from the audio file
     * 2. Runs Gemini 3.8 Flash multimodal audio analysis (with automatic offline DSP fallback if network is unavailable)
     * 3. Synthesizes an immediate cloned voice preview and saves the cloned voice to Room DB
     */
    fun performFullVoiceCloning() {
        val state = _uiState.value
        val draft = state.cloneDraft
        val path = draft.sourceFilePath
        if (path.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    cloneDraft = draft.copy(
                        statusMessage = if (it.isBengali) "অনুগ্রহ করে আগে ভয়েস রেকর্ড করুন বা MP3/অডিও ফাইল দিন।" else "Please record voice or upload an MP3/audio file first.",
                        isError = true
                    )
                )
            }
            return
        }

        val audioFile = File(path)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    cloneDraft = it.cloneDraft.copy(
                        isAnalyzing = true,
                        statusMessage = if (it.isBengali) "🧬 কণ্ঠস্বরের পিচ, ফরমেন্ট ও বাচনভঙ্গি হুবহু ক্লোন করা হচ্ছে..." else "🧬 Cloning vocal pitch, formants, timbre & cadence...",
                        isError = false
                    )
                )
            }

            val dna = draft.extractedDna ?: withContext(Dispatchers.IO) {
                AcousticVoiceDsp.extractVocalDnaFromFile(audioFile)
            }

            val aiResult = aiService.analyzeAudioForVoiceCloning(
                audioFile = audioFile,
                dspMetrics = dna,
                customVoiceName = draft.customVoiceName,
                mimeType = draft.sourceMimeType
            )

            val aiProfile = aiResult.getOrNull()
            val finalNameBn = draft.customVoiceName.ifBlank {
                aiProfile?.suggestedNameBn ?: "ক্লোন করা কণ্ঠ (${dna.fundamentalHz}Hz)"
            }
            val finalNameEn = draft.customVoiceName.ifBlank {
                aiProfile?.suggestedNameEn ?: "Cloned Voice (${dna.fundamentalHz}Hz)"
            }
            val descBn = aiProfile?.descriptionBn?.takeIf { it.isNotBlank() }
                ?: "হুবহু ক্লোন করা কণ্ঠস্বর • মূল ফ্রিকোয়েন্সি ${dna.fundamentalHz} Hz, ফরমেন্ট ${dna.formantShift}x, স্টুডিও ওয়ার্মথ +${dna.warmthDb} dB"
            val descEn = aiProfile?.descriptionEn?.takeIf { it.isNotBlank() }
                ?: "Cloned Vocal DNA • Fundamental ${dna.fundamentalHz} Hz, Formant ${dna.formantShift}x, Warmth +${dna.warmthDb} dB"

            val clonedEntity = ClonedVoiceEntity(
                nameBn = "🧬 $finalNameBn",
                nameEn = "🧬 $finalNameEn",
                isCloned = true,
                gender = aiProfile?.gender ?: dna.detectedGender,
                languageTag = aiProfile?.languageTag ?: if (state.isBengali) "bn-BD" else "en-US",
                basePitch = dna.basePitchRatio,
                baseSpeed = dna.baseSpeedRatio,
                formantShift = dna.formantShift,
                warmthDb = dna.warmthDb,
                clarityDb = dna.clarityDb,
                breathiness = dna.breathiness,
                fundamentalHz = dna.fundamentalHz,
                sampleAudioPath = audioFile.absolutePath,
                descriptionBn = descBn,
                descriptionEn = descEn,
                avatarColorHex = 0xFFFF6D00
            )

            val newVoiceId = repository.saveClonedVoice(clonedEntity)
            val savedVoice = clonedEntity.copy(id = newVoiceId)

            // Generate an immediate sample preview in the newly cloned voice
            val previewText = if (state.isBengali) {
                "হ্যালো! এটি আমার ক্লোন করা নতুন কণ্ঠস্বর। এখন থেকে আপনি এই কণ্ঠ দিয়ে যেকোনো বাংলা বা ইংরেজি টেক্সটকে ভয়েসে রূপান্তর করতে পারবেন।"
            } else {
                "Hello! This is my newly cloned voice profile. You can now use this exact vocal tone for any text-to-speech or voice conversion."
            }

            val previewSynthesis = audioController.synthesizeSpeechToMasteredWav(
                text = previewText,
                voiceProfile = savedVoice,
                emotion = VoiceEmotion.fromKey(aiProfile?.recommendedEmotionKey ?: "NATURAL"),
                userPitchOffset = 1.0f,
                userSpeedOffset = 1.0f,
                userWarmthOffsetDb = 0f,
                userClarityOffsetDb = 0f
            )

            val previewPath = previewSynthesis.getOrNull()?.first?.absolutePath ?: audioFile.absolutePath

            _uiState.update { current ->
                current.copy(
                    selectedVoiceId = newVoiceId,
                    cloneDraft = current.cloneDraft.copy(
                        isAnalyzing = false,
                        extractedDna = dna,
                        aiDescriptionBn = descBn,
                        aiDescriptionEn = descEn,
                        sampleTranscript = aiProfile?.sampleTranscript.orEmpty(),
                        previewFilePath = previewPath,
                        statusMessage = if (current.isBengali) {
                            "🎉 অভিনন্দন! '$finalNameBn' সফলভাবে ক্লোন হয়েছে এবং আপনার ভয়েস লিস্টে যুক্ত হয়েছে!"
                        } else {
                            "🎉 Success! '$finalNameEn' has been cloned and selected for Text-to-Speech!"
                        },
                        isError = false
                    ),
                    bannerMessage = if (current.isBengali) {
                        "🎉 নতুন ক্লোন ভয়েস '$finalNameBn' তৈরি হয়েছে!"
                    } else {
                        "🎉 New Cloned Voice '$finalNameEn' is ready!"
                    },
                    isBannerError = false
                )
            }

            audioController.playAudioFile(previewPath, finalNameBn, dna.waveformPeaks)
        }
    }

    fun deleteClonedVoice(voiceId: Long) {
        viewModelScope.launch {
            repository.deleteClonedVoice(voiceId)
            if (_uiState.value.selectedVoiceId == voiceId) {
                val fallbackId = voices.value.firstOrNull { it.id != voiceId }?.id ?: 1L
                _uiState.update { it.copy(selectedVoiceId = fallbackId) }
            }
        }
    }

    // =========================================================================
    // VOICE CHANGER / VOICE-TO-VOICE CONVERSION FLOWS
    // =========================================================================

    fun setVoiceChangerMode(mode: VoiceChangerMode) {
        _uiState.update {
            it.copy(changerDraft = it.changerDraft.copy(mode = mode))
        }
    }

    fun startVoiceChangerRecording() {
        audioController.stopPlayback()
        val file = audioController.startMicrophoneRecording()
        if (file != null) {
            activeRecordingFile = file
            _uiState.update {
                it.copy(
                    changerDraft = it.changerDraft.copy(
                        isRecording = true,
                        statusMessage = if (it.isBengali) "🎙️ রূপান্তরের জন্য আপনার ভয়েস রেকর্ড হচ্ছে..." else "🎙️ Recording input audio for voice conversion...",
                        isError = false
                    )
                )
            }
        }
    }

    fun stopVoiceChangerRecording() {
        viewModelScope.launch {
            audioController.stopMicrophoneRecording()
            val file = activeRecordingFile
            if (file != null && file.exists()) {
                _uiState.update {
                    it.copy(
                        changerDraft = it.changerDraft.copy(
                            isRecording = false,
                            inputFilePath = file.absolutePath,
                            inputFileName = file.name,
                            inputMimeType = "audio/wav",
                            statusMessage = if (it.isBengali) "✅ অডিও রেকর্ড সম্পন্ন! এখন টার্গেট ভয়েস সিলেক্ট করে 'কণ্ঠ রূপান্তর করুন' চাপুন।" else "✅ Audio recorded! Select target voice and tap Convert.",
                            isError = false
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(changerDraft = it.changerDraft.copy(isRecording = false))
                }
            }
        }
    }

    fun onAudioFilePickedForChanger(uri: Uri) {
        viewModelScope.launch {
            val copiedResult = copyUriToInternalStorage(uri, "voice_samples")
            copiedResult.onSuccess { (file, displayName, mimeType) ->
                _uiState.update { state ->
                    state.copy(
                        changerDraft = state.changerDraft.copy(
                            inputFilePath = file.absolutePath,
                            inputFileName = displayName,
                            inputMimeType = mimeType,
                            statusMessage = if (state.isBengali) "✅ '$displayName' লোড হয়েছে। এখন পছন্দের কণ্ঠে রূপান্তর করুন।" else "✅ Loaded '$displayName'. Ready to convert!",
                            isError = false
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update { state ->
                    state.copy(
                        changerDraft = state.changerDraft.copy(
                            statusMessage = err.localizedMessage ?: "Failed to load audio",
                            isError = true
                        )
                    )
                }
            }
        }
    }

    fun convertVoiceToSelectedTarget() {
        val state = _uiState.value
        val draft = state.changerDraft
        val inputPath = draft.inputFilePath
        if (inputPath.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    changerDraft = draft.copy(
                        statusMessage = if (it.isBengali) "অনুগ্রহ করে আগে ভয়েস রেকর্ড করুন বা অডিও ফাইল সিলেক্ট করুন।" else "Please record or upload an audio file first.",
                        isError = true
                    )
                )
            }
            return
        }

        val sourceFile = File(inputPath)
        val targetVoice = getSelectedVoice()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    changerDraft = it.changerDraft.copy(
                        isConverting = true,
                        statusMessage = if (it.isBengali) {
                            "🔄 '${targetVoice.nameBn}' কণ্ঠে অডিও রূপান্তর করা হচ্ছে..."
                        } else {
                            "🔄 Transforming voice into '${targetVoice.nameEn}'..."
                        },
                        isError = false
                    )
                )
            }

            if (draft.mode == VoiceChangerMode.AI_SMART_RESYNTHESIS) {
                // Transcribe via Gemini + Re-synthesize in target/cloned voice
                val transResult = aiService.transcribeAudioForVoiceChanger(
                    audioFile = sourceFile,
                    mimeType = draft.inputMimeType
                )
                val transcript = transResult.getOrNull()?.transcriptText?.takeIf { it.isNotBlank() }

                if (transcript != null) {
                    val synthResult = audioController.synthesizeSpeechToMasteredWav(
                        text = transcript,
                        voiceProfile = targetVoice,
                        emotion = state.selectedEmotion,
                        userPitchOffset = state.userPitchMultiplier,
                        userSpeedOffset = state.userSpeedMultiplier,
                        userWarmthOffsetDb = state.userWarmthOffsetDb,
                        userClarityOffsetDb = state.userClarityOffsetDb
                    )
                    synthResult.onSuccess { (outFile, peaks) ->
                        saveAndPlayConvertedVoice(outFile, peaks, transcript, targetVoice)
                        return@launch
                    }
                }
            }

            // Direct Acoustic Morphing (works 100% offline on any MP3/WAV recording)
            val morphResult = audioController.morphAudioToTargetVoiceDirectly(
                sourceFile = sourceFile,
                targetVoice = targetVoice,
                emotion = state.selectedEmotion
            )
            morphResult.onSuccess { (outFile, peaks) ->
                val label = if (state.isBengali) {
                    "অ্যাকোস্টিক ভয়েস রূপান্তর (${draft.inputFileName})"
                } else {
                    "Acoustic Voice Morph (${draft.inputFileName})"
                }
                saveAndPlayConvertedVoice(outFile, peaks, label, targetVoice)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        changerDraft = it.changerDraft.copy(
                            isConverting = false,
                            statusMessage = err.localizedMessage ?: "Voice conversion failed",
                            isError = true
                        )
                    )
                }
            }
        }
    }

    private suspend fun saveAndPlayConvertedVoice(
        outFile: File,
        peaks: List<Float>,
        transcriptOrLabel: String,
        targetVoice: ClonedVoiceEntity
    ) {
        val dur = AcousticVoiceDsp.extractVocalDnaFromFile(outFile).durationMs
        val title = "🔄 ${targetVoice.nameBn.take(18)}"
        repository.saveGeneratedAudio(
            GeneratedAudioEntity(
                title = title,
                scriptText = transcriptOrLabel,
                voiceNameBn = targetVoice.nameBn,
                voiceNameEn = targetVoice.nameEn,
                emotionKey = _uiState.value.selectedEmotion.key,
                audioFilePath = outFile.absolutePath,
                durationMs = dur,
                isVoiceChanged = true
            )
        )
        _uiState.update { state ->
            state.copy(
                changerDraft = state.changerDraft.copy(
                    isConverting = false,
                    transcribedText = transcriptOrLabel,
                    outputFilePath = outFile.absolutePath,
                    outputWaveformPeaks = peaks,
                    statusMessage = if (state.isBengali) {
                        "✅ কণ্ঠস্বর সফলভাবে '${targetVoice.nameBn}'-এ রূপান্তরিত হয়েছে!"
                    } else {
                        "✅ Voice transformed into '${targetVoice.nameEn}'!"
                    },
                    isError = false
                )
            )
        }
        audioController.playAudioFile(outFile.absolutePath, title, peaks)
    }

    // =========================================================================
    // EXPORT, SHARE & LIBRARY MANAGEMENT
    // =========================================================================

    fun downloadAudioFile(filePath: String, title: String) {
        viewModelScope.launch {
            val res = audioController.saveAudioToDownloads(filePath, title)
            res.onSuccess { savedLocation ->
                _uiState.update {
                    it.copy(
                        bannerMessage = if (it.isBengali) {
                            "📥 অডিও ফাইল সেভ হয়েছে: $savedLocation"
                        } else {
                            "📥 Audio saved to: $savedLocation"
                        },
                        isBannerError = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        bannerMessage = err.localizedMessage ?: "Failed to save file",
                        isBannerError = true
                    )
                }
            }
        }
    }

    fun shareAudioFile(filePath: String, title: String) {
        audioController.shareAudioFile(filePath, title)
    }

    fun deleteGeneratedAudio(audioId: Long) {
        viewModelScope.launch {
            repository.deleteGeneratedAudio(audioId)
        }
    }

    private suspend fun copyUriToInternalStorage(
        uri: Uri,
        folderName: String
    ): Result<Triple<File, String, String>> = withContext(Dispatchers.IO) {
        try {
            val resolver = getApplication<Application>().contentResolver
            var displayName = "uploaded_voice_${System.currentTimeMillis()}.mp3"
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }
            }
            val mimeType = resolver.getType(uri) ?: when {
                displayName.endsWith(".mp3", true) -> "audio/mp3"
                displayName.endsWith(".m4a", true) -> "audio/mp4"
                else -> "audio/wav"
            }

            val ext = displayName.substringAfterLast(".", "mp3").take(4)
            val destDir = File(getApplication<Application>().filesDir, folderName).apply { mkdirs() }
            val destFile = File(destDir, "import_${System.currentTimeMillis()}.$ext")

            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open selected audio file"))

            Result.success(Triple(destFile, displayName, mimeType))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy audio URI to internal storage", e)
            Result.failure(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioController.release()
    }
}
