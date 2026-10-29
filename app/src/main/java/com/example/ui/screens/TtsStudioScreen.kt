package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.PlaybackState
import com.example.data.ClonedVoiceEntity
import com.example.data.VoiceEmotion
import com.example.ui.components.InteractiveWaveformPlayerCard
import com.example.ui.components.StudioHeroBanner
import com.example.ui.components.VoiceProfileCarousel
import com.example.ui.theme.CoralCloneSecondary
import com.example.ui.theme.CyanWavePrimary
import com.example.ui.theme.StudioCardElevatedDark
import com.example.ui.theme.VioletAiTertiary
import com.example.viewmodel.StudioUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TtsStudioScreen(
    uiState: StudioUiState,
    voices: List<ClonedVoiceEntity>,
    playbackState: PlaybackState,
    onScriptChange: (String) -> Unit,
    onSelectVoice: (Long) -> Unit,
    onDeleteClonedVoice: (Long) -> Unit,
    onSelectEmotion: (VoiceEmotion) -> Unit,
    onPitchChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onWarmthChange: (Float) -> Unit,
    onClarityChange: (Float) -> Unit,
    onResetTuning: () -> Unit,
    onGenerateVoice: () -> Unit,
    onGenerateAiScript: (String, String) -> Unit,
    onPlaySample: (String, String) -> Unit,
    onPlayPauseLatest: () -> Unit,
    onSeekLatest: (Float) -> Unit,
    onDownloadLatest: () -> Unit,
    onShareLatest: () -> Unit,
    onNavigateToClone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn = uiState.isBengali
    val clonedCount = voices.count { it.isCloned }
    var showAiScriptDialog by remember { mutableStateOf(false) }
    var showAdvancedSliders by remember { mutableStateOf(true) }

    val quickSamplePrompts = if (isBn) {
        listOf(
            "🎬 ইউটিউব ইন্ট্রো" to "হ্যালো বন্ধুরা! আজকের ভিডিওতে আমরা এমন একটি চমৎকার প্রযুক্তি নিয়ে কথা বলব, যা আপনার কন্টেন্ট তৈরির অভিজ্ঞতা সম্পূর্ণ বদলে দেবে।",
            "📖 অনুপ্রেরণার গল্প" to "জীবনের প্রতিটি কঠিন মুহূর্ত আমাদের নতুন কিছু শেখায়। কখনো হাল ছেড়ে দেবেন না, কারণ অন্ধকারের পরেই আসে নতুন ভোরের আলো।",
            "📺 ব্রেকিং নিউজ" to "শুভ সন্ধ্যা, সংবাদে স্বাগতম। আজ বিজ্ঞান ও প্রযুক্তির জগতে এক নতুন দিগন্তের সূচনা হয়েছে, যেখানে কৃত্রিম বুদ্ধিমত্তা মানুষের কণ্ঠস্বরকে আরও জীবন্ত করে তুলছে।",
            "🎙️ বিজ্ঞাপন ভয়েস" to "আপনার ব্র্যান্ড বা ব্যবসার জন্য আকর্ষণীয় ভয়েসওভার খুঁজছেন? কণ্ঠ স্টুডিও অ্যাপের মাধ্যমে মাত্র এক ক্লিকেই তৈরি করুন প্রফেশনাল ভয়েস!"
        )
    } else {
        listOf(
            "🎬 YouTube Intro" to "Hey everyone! Welcome back to the channel. Today we are exploring a groundbreaking voice studio that transforms any script into lifelike speech.",
            "📖 Storyteller" to "Every great journey begins with a single step into the unknown. Even in the quietest night, the stars remind us that light never truly fades.",
            "📺 News Broadcast" to "Good evening. Breaking developments in acoustic artificial intelligence are empowering creators worldwide to produce broadcast-quality audio from their phones.",
            "🎙️ Commercial Ad" to "Looking for the perfect studio voice for your next video? With KobiVoice Studio, clone any voice and create studio-grade narration in seconds!"
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tts_studio_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StudioHeroBanner(
                isBengali = isBn,
                clonedCount = clonedCount,
                onQuickCloneClick = onNavigateToClone
            )
        }

        // Latest Generated Audio Player (if available)
        if (!uiState.latestGeneratedAudioPath.isNullOrBlank()) {
            item {
                val selectedVoice = voices.find { it.id == uiState.selectedVoiceId }
                InteractiveWaveformPlayerCard(
                    title = uiState.latestGeneratedTitle.ifBlank {
                        if (isBn) "তৈরি করা ভয়েসওভার" else "Generated Voiceover"
                    },
                    subtitle = if (isBn) {
                        "${selectedVoice?.nameBn ?: ""} • ${uiState.selectedEmotion.labelBn}"
                    } else {
                        "${selectedVoice?.nameEn ?: ""} • ${uiState.selectedEmotion.labelEn}"
                    },
                    filePath = uiState.latestGeneratedAudioPath,
                    waveformPeaks = uiState.latestWaveformPeaks,
                    playbackState = playbackState,
                    isBengali = isBn,
                    onPlayPause = onPlayPauseLatest,
                    onSeek = onSeekLatest,
                    onDownload = onDownloadLatest,
                    onShare = onShareLatest
                )
            }
        }

        // Step 1: Voice Selection Carousel
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "১. কণ্ঠস্বর নির্বাচন করুন (মানুষ বা ক্লোন ভয়েস)" else "1. Select Speaker Voice (Human or Cloned)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = onNavigateToClone,
                        modifier = Modifier.testTag("add_clone_voice_btn")
                    ) {
                        Text(
                            text = if (isBn) "+ নতুন ক্লোন" else "+ Clone New",
                            color = CoralCloneSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                VoiceProfileCarousel(
                    voices = voices,
                    selectedVoiceId = uiState.selectedVoiceId,
                    isBengali = isBn,
                    onSelectVoice = onSelectVoice,
                    onPreviewSample = onPlaySample,
                    onDeleteClonedVoice = onDeleteClonedVoice
                )
            }
        }

        // Step 2: Script Editor Card + AI Script Assistant
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevatedDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "২. আপনার টেক্সট বা স্ক্রিপ্ট লিখুন" else "2. Enter Your Script (Bengali or English)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = { showAiScriptDialog = true },
                            enabled = !uiState.isGeneratingScript,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VioletAiTertiary.copy(alpha = 0.2f),
                                contentColor = VioletAiTertiary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("ai_script_writer_button")
                        ) {
                            if (uiState.isGeneratingScript) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = VioletAiTertiary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "এআই স্ক্রিপ্ট" else "AI Script",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Sample Templates
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickSamplePrompts.forEach { (label, sampleText) ->
                            Surface(
                                modifier = Modifier.clickable { onScriptChange(sampleText) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.scriptText,
                        onValueChange = onScriptChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("tts_script_input"),
                        placeholder = {
                            Text(
                                text = if (isBn) {
                                    "এখানে বাংলা বা ইংরেজিতে যেকোনো লেখা টাইপ বা পেস্ট করুন..."
                                } else {
                                    "Type or paste your Bengali or English script here..."
                                }
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanWavePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) {
                                "অক্ষর সংখ্যা: ${uiState.scriptText.length}"
                            } else {
                                "Characters: ${uiState.scriptText.length}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (uiState.scriptText.isNotEmpty()) {
                            TextButton(
                                onClick = { onScriptChange("") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear script",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isBn) "মুছে ফেলুন" else "Clear")
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Emotion & Acoustic Voice Tuning Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevatedDark),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyanWavePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "৩. কণ্ঠের আবেগ, পিচ ও গম্ভীরতা টিউনিং" else "3. Vocal Emotion, Pitch & Timbre Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = onResetTuning) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = if (isBn) "রিসেট করুন" else "Reset Sliders",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Emotion Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VoiceEmotion.entries.forEach { emotion ->
                            val selected = uiState.selectedEmotion == emotion
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectEmotion(emotion) },
                                label = {
                                    Text(
                                        text = "${emotion.emoji} ${if (isBn) emotion.labelBn else emotion.labelEn}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanWavePrimary.copy(alpha = 0.22f),
                                    selectedLabelColor = CyanWavePrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = if (selected) CyanWavePrimary else MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedVisibility(visible = showAdvancedSliders) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Pitch Slider
                            TuningSliderRow(
                                label = if (isBn) "কণ্ঠের পিচ (Pitch)" else "Voice Pitch",
                                valueText = "${(uiState.userPitchMultiplier * 100).roundToInt()}%",
                                value = uiState.userPitchMultiplier,
                                valueRange = 0.65f..1.45f,
                                onValueChange = onPitchChange,
                                accentColor = CyanWavePrimary
                            )

                            // Speed Slider
                            TuningSliderRow(
                                label = if (isBn) "কথা বলার গতি (Speed)" else "Speaking Speed",
                                valueText = "${(uiState.userSpeedMultiplier * 100).roundToInt()}%",
                                value = uiState.userSpeedMultiplier,
                                valueRange = 0.65f..1.45f,
                                onValueChange = onSpeedChange,
                                accentColor = CyanWavePrimary
                            )

                            // Warmth / Deep Bass Slider
                            TuningSliderRow(
                                label = if (isBn) "কণ্ঠের গভীরতা ও বেস (Warmth)" else "Chest Warmth & Bass",
                                valueText = "${if (uiState.userWarmthOffsetDb >= 0) "+" else ""}${uiState.userWarmthOffsetDb.roundToInt()} dB",
                                value = uiState.userWarmthOffsetDb,
                                valueRange = -5f..8f,
                                onValueChange = onWarmthChange,
                                accentColor = CoralCloneSecondary
                            )

                            // Studio Clarity Slider
                            TuningSliderRow(
                                label = if (isBn) "স্টুডিও ক্লিয়ারিটি (HD Clarity)" else "Studio Presence & Clarity",
                                valueText = "${if (uiState.userClarityOffsetDb >= 0) "+" else ""}${uiState.userClarityOffsetDb.roundToInt()} dB",
                                value = uiState.userClarityOffsetDb,
                                valueRange = -4f..8f,
                                onValueChange = onClarityChange,
                                accentColor = VioletAiTertiary
                            )
                        }
                    }
                }
            }
        }

        // Primary Generate Voice Button
        item {
            Button(
                onClick = onGenerateVoice,
                enabled = !uiState.isSynthesizing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("generate_voice_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanWavePrimary,
                    contentColor = Color(0xFF00222B)
                )
            ) {
                if (uiState.isSynthesizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF00222B),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isBn) "হাই-কোয়ালিটি ভয়েস তৈরি হচ্ছে..." else "Synthesizing Studio Voice...",
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
                        text = if (isBn) "🔊 ভয়েস তৈরি করুন (Generate Voice)" else "🔊 Generate Realistic Voice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAiScriptDialog) {
        AiScriptAssistantDialog(
            isBengali = isBn,
            initialTopic = uiState.scriptText,
            onDismiss = { showAiScriptDialog = false },
            onGenerate = { topic, style ->
                showAiScriptDialog = false
                onGenerateAiScript(topic, style)
            }
        )
    }
}

@Composable
private fun TuningSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = accentColor.copy(alpha = 0.22f)
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiScriptAssistantDialog(
    isBengali: Boolean,
    initialTopic: String,
    onDismiss: () -> Unit,
    onGenerate: (String, String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    val categories = if (isBengali) {
        listOf("ইউটিউব ভিডিও ইন্ট্রো", "অনুপ্রেরণামূলক গল্প", "সংবাদ প্রতিবেদন", "বিজ্ঞাপন প্রমো", "রহস্য ও থ্রিলার")
    } else {
        listOf("YouTube Video Intro", "Motivational Story", "News Report", "Commercial Promo", "Mystery Narration")
    }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBengali) "✨ এআই ভয়েসওভার স্ক্রিপ্ট রাইটার" else "✨ AI Voiceover Script Writer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isBengali) {
                        "আপনি কোন বিষয়ে ভয়েসওভার করতে চান তা সংক্ষেপে লিখুন, Gemini AI সুন্দর স্ক্রিপ্ট সাজিয়ে দেবে:"
                    } else {
                        "Describe your topic or paste rough notes, and Gemini AI will craft a voiceover script:"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    placeholder = {
                        Text(
                            text = if (isBengali) {
                                "যেমন: মহাকাশ ভ্রমণের রহস্য বা নতুন স্মার্টফোন রিভিউ..."
                            } else {
                                "e.g., Mystery of deep space or smartphone review..."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Text(
                    text = if (isBengali) "স্ক্রিপ্টের ধরন:" else "Script Style:",
                    style = MaterialTheme.typography.labelLarge
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerate(topic.ifBlank { initialTopic }, selectedCategory)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanWavePrimary,
                    contentColor = Color(0xFF00222B)
                )
            ) {
                Text(
                    text = if (isBengali) "স্ক্রিপ্ট তৈরি করুন" else "Generate Script",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}
