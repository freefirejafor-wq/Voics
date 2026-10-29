package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.PlaybackState
import com.example.data.ClonedVoiceEntity
import com.example.ui.components.InteractiveWaveformPlayerCard
import com.example.ui.components.LiveRecordingVisualizer
import com.example.ui.components.VoiceProfileCarousel
import com.example.ui.theme.CoralCloneSecondary
import com.example.ui.theme.CyanWavePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioCardElevatedDark
import com.example.ui.theme.VioletAiTertiary
import com.example.viewmodel.VoiceChangerDraftState
import com.example.viewmodel.VoiceChangerMode

@Composable
fun VoiceChangerScreen(
    isBengali: Boolean,
    changerDraft: VoiceChangerDraftState,
    voices: List<ClonedVoiceEntity>,
    selectedVoiceId: Long,
    recordingAmplitude: Float,
    recordingSeconds: Int,
    playbackState: PlaybackState,
    onSelectTargetVoice: (Long) -> Unit,
    onSelectMode: (VoiceChangerMode) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPickAudioFile: () -> Unit,
    onConvertVoice: () -> Unit,
    onPlayAudio: (String, String) -> Unit,
    onSeekAudio: (Float) -> Unit,
    onDownloadAudio: (String, String) -> Unit,
    onShareAudio: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("voice_changer_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14222E)),
                border = BorderStroke(1.2.dp, CyanWavePrimary.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isBengali) "🔄 রিয়েলিস্টিক ভয়েস টু ভয়েস রূপান্তর (Voice Changer)" else "🔄 Realistic Voice-to-Voice Converter",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBengali) {
                            "যেকোনো ভয়েস রেকর্ড করুন বা MP3 অডিও ফাইল দিন এবং সেটিকে অন্য মানুষের কণ্ঠে বা আপনার ক্লোন করা কণ্ঠে রূপান্তর করুন।"
                        } else {
                            "Record your voice or upload an MP3 and transform it into any realistic human or cloned voice profile."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Step 1: Record or Upload Source Audio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevatedDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBengali) "১. যে ভয়েসটি পরিবর্তন করতে চান সেটি দিন" else "1. Record or Upload Source Voice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (changerDraft.isRecording) {
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
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralCloneSecondary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBengali) "রেকর্ডিং থামান (Stop)" else "Stop Recording",
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
                                    .height(54.dp)
                                    .testTag("changer_record_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralCloneSecondary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBengali) "ভয়েস রেকর্ড" else "Record Audio",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onPickAudioFile,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("changer_upload_btn"),
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
                                    text = if (isBengali) "MP3/অডিও দিন" else "Upload MP3",
                                    color = CyanWavePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (!changerDraft.inputFilePath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
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
                                    Text(
                                        text = changerDraft.inputFileName.ifBlank { "input_audio.wav" },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        onPlayAudio(
                                            changerDraft.inputFilePath,
                                            changerDraft.inputFileName.ifBlank { "Input Audio" }
                                        )
                                    }
                                ) {
                                    Text(
                                        text = if (isBengali) "আসল শুনুন" else "Play Input",
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Select Target Voice (Human or Cloned)
        item {
            Column {
                Text(
                    text = if (isBengali) "২. কোন কণ্ঠে রূপান্তর করতে চান তা বেছে নিন" else "2. Choose Target Voice (Human or Cloned)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                VoiceProfileCarousel(
                    voices = voices,
                    selectedVoiceId = selectedVoiceId,
                    isBengali = isBengali,
                    onSelectVoice = onSelectTargetVoice,
                    onPreviewSample = onPlayAudio
                )
            }
        }

        // Step 3: Conversion Mode Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevatedDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBengali) "৩. ভয়েস রূপান্তর প্রযুক্তি" else "3. Conversion Engine Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isMorph = changerDraft.mode == VoiceChangerMode.DIRECT_ACOUSTIC_MORPH
                        FilterChip(
                            selected = isMorph,
                            onClick = { onSelectMode(VoiceChangerMode.DIRECT_ACOUSTIC_MORPH) },
                            label = {
                                Text(
                                    text = if (isBengali) "🎛️ ডিরেক্ট অ্যাকোস্টিক মর্ফ" else "🎛️ Direct Acoustic Morph"
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanWavePrimary.copy(alpha = 0.22f),
                                selectedLabelColor = CyanWavePrimary
                            )
                        )

                        val isAi = changerDraft.mode == VoiceChangerMode.AI_SMART_RESYNTHESIS
                        FilterChip(
                            selected = isAi,
                            onClick = { onSelectMode(VoiceChangerMode.AI_SMART_RESYNTHESIS) },
                            label = {
                                Text(
                                    text = if (isBengali) "✨ এআই স্মার্ট রি-সিনথেসিস" else "✨ AI Smart Re-Synthesis"
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletAiTertiary.copy(alpha = 0.22f),
                                selectedLabelColor = VioletAiTertiary
                            )
                        )
                    }
                }
            }
        }

        if (!changerDraft.statusMessage.isNullOrBlank()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (changerDraft.isError) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                    } else {
                        CyanWavePrimary.copy(alpha = 0.14f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (changerDraft.isError) MaterialTheme.colorScheme.error else CyanWavePrimary
                    )
                ) {
                    Text(
                        text = changerDraft.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Convert Button
        item {
            Button(
                onClick = onConvertVoice,
                enabled = !changerDraft.isConverting && !changerDraft.isRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("convert_voice_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanWavePrimary,
                    contentColor = Color(0xFF00222B)
                )
            ) {
                if (changerDraft.isConverting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF00222B),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isBengali) "কণ্ঠস্বর রূপান্তর হচ্ছে..." else "Converting Voice...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(imageVector = Icons.Default.Transform, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBengali) "🔄 কণ্ঠ রূপান্তর করুন (Convert Voice)" else "🔄 Transform Voice Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Output Player Card
        if (!changerDraft.outputFilePath.isNullOrBlank()) {
            item {
                val targetVoice = voices.find { it.id == selectedVoiceId }
                InteractiveWaveformPlayerCard(
                    title = if (isBengali) {
                        "রূপান্তরিত ভয়েস (${targetVoice?.nameBn ?: ""})"
                    } else {
                        "Converted Voice (${targetVoice?.nameEn ?: ""})"
                    },
                    subtitle = changerDraft.transcribedText,
                    filePath = changerDraft.outputFilePath,
                    waveformPeaks = changerDraft.outputWaveformPeaks,
                    playbackState = playbackState,
                    isBengali = isBengali,
                    onPlayPause = {
                        onPlayAudio(
                            changerDraft.outputFilePath,
                            targetVoice?.nameBn ?: "Converted Voice"
                        )
                    },
                    onSeek = onSeekAudio,
                    onDownload = {
                        onDownloadAudio(
                            changerDraft.outputFilePath,
                            "converted_${targetVoice?.nameEn ?: "voice"}"
                        )
                    },
                    onShare = {
                        onShareAudio(
                            changerDraft.outputFilePath,
                            targetVoice?.nameBn ?: "Converted Voice"
                        )
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
