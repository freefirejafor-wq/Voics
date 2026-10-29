package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.PlaybackState
import com.example.audio.VocalDnaMetrics
import com.example.ui.components.InteractiveWaveformPlayerCard
import com.example.ui.components.LiveRecordingVisualizer
import com.example.ui.theme.CoralCloneSecondary
import com.example.ui.theme.CyanWavePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioCardElevatedDark
import com.example.ui.theme.VioletAiTertiary
import com.example.viewmodel.CloneDraftState

@Composable
fun VoiceCloneScreen(
    isBengali: Boolean,
    cloneDraft: CloneDraftState,
    recordingAmplitude: Float,
    recordingSeconds: Int,
    playbackState: PlaybackState,
    onVoiceNameChange: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPickAudioFile: () -> Unit,
    onPerformVoiceClone: () -> Unit,
    onPlayPreview: (String, String) -> Unit,
    onSeekPreview: (Float) -> Unit,
    onDownloadPreview: (String, String) -> Unit,
    onSharePreview: (String, String) -> Unit,
    onUseClonedVoiceInTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("voice_clone_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Explanation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1629)),
                border = BorderStroke(1.2.dp, CoralCloneSecondary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CoralCloneSecondary.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = CoralCloneSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBengali) "🧬 হুবহু মানুষের কণ্ঠস্বর ক্লোন স্টুডিও" else "🧬 Instant Human Voice Cloning Studio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBengali) {
                                    "MP3/WAV অডিও ফাইল দিন অথবা সরাসরি কথা রেকর্ড করে হুবহু সেই কণ্ঠ কপি করুন"
                                } else {
                                    "Upload any MP3/WAV voice clip or record live to clone pitch, formants & timbre"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Step 1: Name the Cloned Voice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevatedDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBengali) "১. ক্লোন করা কণ্ঠের একটি নাম দিন" else "1. Name Your Cloned Voice Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cloneDraft.customVoiceName,
                        onValueChange = onVoiceNameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clone_voice_name_input"),
                        placeholder = {
                            Text(
                                text = if (isBengali) {
                                    "যেমন: আমার নিজের কণ্ঠ / সাকিবের ভয়েস..."
                                } else {
                                    "e.g., My Studio Voice / Narrator Clone..."
                                }
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralCloneSecondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // Step 2: Upload MP3/Audio OR Record Live with Guided Script
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevatedDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBengali) {
                            "২. ভয়েস স্যাম্পল দিন (MP3 ফাইল আপলোড বা সরাসরি রেকর্ড)"
                        } else {
                            "2. Provide Voice Sample (Upload MP3/Audio or Record Mic)"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBengali) {
                            "ভালো ফলাফলের জন্য ১০ থেকে ৩০ সেকেন্ডের স্পষ্ট কণ্ঠস্বর রেকর্ড করুন বা যেকোনো অডিও ফাইল সিলেক্ট করুন।"
                        } else {
                            "For best accuracy, upload a clean 10-30s MP3/WAV clip or read the guided script aloud."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Guided Reading Script Box for Live Recording
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF101824),
                        border = BorderStroke(1.dp, CyanWavePrimary.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isBengali) "📖 রেকর্ড করার জন্য নমুনা স্ক্রিপ্ট (দেখে পড়ুন):" else "📖 Guided Reading Script (Read Aloud):",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanWavePrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBengali) {
                                    "“আমি কণ্ঠ স্টুডিও অ্যাপে আমার নিজের কণ্ঠস্বর রেকর্ড করছি। আকাশে আজ মেঘের খেলা, আর বাতাসে ভেসে আসছে নতুন দিনের গান। এই কণ্ঠস্বর দিয়ে যেকোনো লেখা খুব সহজেই ভয়েসে রূপান্তর করা যাবে।”"
                                } else {
                                    "“I am recording my natural speaking voice in KobiVoice Studio. Every voice carries a unique rhythm, warmth, and melody that makes stories come alive.”"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (cloneDraft.isRecording) {
                        LiveRecordingVisualizer(
                            amplitude = recordingAmplitude,
                            secondsElapsed = recordingSeconds,
                            isBengali = isBengali
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onStopRecording,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("stop_clone_recording_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralCloneSecondary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "রেকর্ডিং সম্পন্ন করুন (Stop)" else "Stop & Save Recording",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onStartRecording,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("start_clone_recording_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralCloneSecondary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "ভয়েস রেকর্ড" else "Record Mic",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onPickAudioFile,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("upload_clone_audio_btn"),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, CyanWavePrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = CyanWavePrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "MP3/অডিও ফাইল" else "Upload MP3",
                                    color = CyanWavePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Show loaded file & Vocal DNA Telemetry if a sample is present
                    if (!cloneDraft.sourceFilePath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = EmeraldSuccess.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AudioFile,
                                        contentDescription = null,
                                        tint = EmeraldSuccess
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cloneDraft.sourceFileName.ifBlank { "voice_sample.wav" },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isBengali) "কণ্ঠের অডিও ফাইল প্রস্তুত" else "Voice sample ready for cloning",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldSuccess
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        onPlayPreview(
                                            cloneDraft.sourceFilePath,
                                            cloneDraft.sourceFileName.ifBlank { "Original Sample" }
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isBengali) "শুনুন" else "Play",
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Vocal DNA Acoustic Telemetry Card (when extracted)
        if (cloneDraft.extractedDna != null) {
            item {
                VocalDnaTelemetryCard(
                    dna = cloneDraft.extractedDna,
                    aiDescription = if (isBengali) cloneDraft.aiDescriptionBn else cloneDraft.aiDescriptionEn,
                    sampleTranscript = cloneDraft.sampleTranscript,
                    isBengali = isBengali
                )
            }
        }

        // Status Banner
        if (!cloneDraft.statusMessage.isNullOrBlank()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (cloneDraft.isError) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                    } else {
                        CyanWavePrimary.copy(alpha = 0.14f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (cloneDraft.isError) MaterialTheme.colorScheme.error else CyanWavePrimary
                    )
                ) {
                    Text(
                        text = cloneDraft.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Step 3: Clone Action Button
        item {
            Button(
                onClick = onPerformVoiceClone,
                enabled = !cloneDraft.isAnalyzing && !cloneDraft.isRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("execute_voice_clone_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralCloneSecondary,
                    contentColor = Color.White
                )
            ) {
                if (cloneDraft.isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isBengali) "কণ্ঠের ডিএনএ বিশ্লেষণ ও ক্লোন হচ্ছে..." else "Analyzing & Cloning Vocal DNA...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBengali) "🧬 হুবহু ভয়েস ক্লোন করুন (Clone Voice Now)" else "🧬 Clone This Voice Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Preview Cloned Voice Card + "Use in Text-to-Voice" CTA
        if (!cloneDraft.previewFilePath.isNullOrBlank()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InteractiveWaveformPlayerCard(
                        title = if (isBengali) {
                            "ক্লোন করা কণ্ঠের প্রিভিউ (${cloneDraft.customVoiceName.ifBlank { "নতুন ক্লোন" }})"
                        } else {
                            "Cloned Voice Preview (${cloneDraft.customVoiceName.ifBlank { "New Clone" }})"
                        },
                        subtitle = if (isBengali) "হুবহু ক্লোন করা পিচ ও ফরমেন্ট অডিও" else "Synthesized with cloned vocal DNA",
                        filePath = cloneDraft.previewFilePath,
                        waveformPeaks = cloneDraft.extractedDna?.waveformPeaks ?: emptyList(),
                        playbackState = playbackState,
                        isBengali = isBengali,
                        onPlayPause = {
                            onPlayPreview(
                                cloneDraft.previewFilePath,
                                cloneDraft.customVoiceName.ifBlank { "Cloned Voice" }
                            )
                        },
                        onSeek = onSeekPreview,
                        onDownload = {
                            onDownloadPreview(
                                cloneDraft.previewFilePath,
                                cloneDraft.customVoiceName.ifBlank { "cloned_voice" }
                            )
                        },
                        onShare = {
                            onSharePreview(
                                cloneDraft.previewFilePath,
                                cloneDraft.customVoiceName.ifBlank { "Cloned Voice" }
                            )
                        }
                    )

                    Button(
                        onClick = onUseClonedVoiceInTts,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("use_cloned_voice_in_tts_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color(0xFF002411)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) {
                                "এই ক্লোন ভয়েস দিয়ে টেক্সট-টু-ভয়েস তৈরি করুন →"
                            } else {
                                "Use This Cloned Voice in Text-to-Speech →"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun VocalDnaTelemetryCard(
    dna: VocalDnaMetrics,
    aiDescription: String,
    sampleTranscript: String,
    isBengali: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2B)),
        border = BorderStroke(1.dp, VioletAiTertiary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isBengali) "🔬 শনাক্তকৃত কণ্ঠস্বরের অ্যাকোস্টিক ডিএনএ (Vocal DNA)" else "🔬 Extracted Vocal DNA Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VioletAiTertiary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DnaMetricPill(
                    label = if (isBengali) "মূল ফ্রিকোয়েন্সি (F0)" else "Fundamental F0",
                    value = "${dna.fundamentalHz} Hz",
                    color = CyanWavePrimary,
                    modifier = Modifier.weight(1f)
                )
                DnaMetricPill(
                    label = if (isBengali) "ফরমেন্ট ডেপথ" else "Formant Shift",
                    value = "${dna.formantShift}x",
                    color = CoralCloneSecondary,
                    modifier = Modifier.weight(1f)
                )
                DnaMetricPill(
                    label = if (isBengali) "স্টুডিও ওয়ার্মথ" else "Warmth EQ",
                    value = "+${dna.warmthDb} dB",
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }

            if (aiDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = aiDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (sampleTranscript.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBengali) "শনাক্তকৃত কথা: “$sampleTranscript”" else "Detected Speech: “$sampleTranscript”",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DnaMetricPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
