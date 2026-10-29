package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.PlaybackState
import com.example.data.ClonedVoiceEntity
import com.example.ui.theme.CoralCloneSecondary
import com.example.ui.theme.CyanWavePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioCardElevatedDark
import com.example.ui.theme.VioletAiTertiary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun StudioHeroBanner(
    isBengali: Boolean,
    clonedCount: Int,
    onQuickCloneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studio_hero_banner"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = BorderStroke(1.dp, CyanWavePrimary.copy(alpha = 0.28f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Animated acoustic DNA wave canvas background
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            ) {
                val w = size.width
                val h = size.height
                val midY = h * 0.55f

                val path1 = Path()
                val path2 = Path()
                val steps = 60
                for (i in 0..steps) {
                    val x = (w / steps) * i
                    val norm = i.toFloat() / steps
                    val env = sin(norm * PI).toFloat()
                    val y1 = midY + sin(norm * 10f + phase) * (h * 0.24f) * env
                    val y2 = midY + sin(norm * 8f - phase * 1.2f) * (h * 0.18f) * env
                    if (i == 0) {
                        path1.moveTo(x, y1)
                        path2.moveTo(x, y2)
                    } else {
                        path1.lineTo(x, y1)
                        path2.lineTo(x, y2)
                    }
                }
                drawPath(
                    path = path1,
                    color = CyanWavePrimary.copy(alpha = 0.22f),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawPath(
                    path = path2,
                    color = CoralCloneSecondary.copy(alpha = 0.20f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CyanWavePrimary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, CyanWavePrimary.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = if (isBengali) "🧬 ১০০% ফ্রি এআই ভয়েস ইঞ্জিন" else "🧬 100% Free AI Voice Studio",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanWavePrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        if (clonedCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = CoralCloneSecondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (isBengali) "$clonedCount টি ক্লোন ভয়েস" else "$clonedCount Cloned",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CoralCloneSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) {
                            "টেক্সট থেকে রিয়েলিস্টিক মানুষের কণ্ঠ ও হুবহু ভয়েস ক্লোনিং"
                        } else {
                            "Realistic Text-to-Voice & Exact MP3 Voice Cloning"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBengali) {
                            "যেকোনো MP3 বা রেকর্ড করা ভয়েস থেকে হুবহু কণ্ঠ কপি করে ব্যবহার করুন"
                        } else {
                            "Copy any speaker's pitch, timbre & cadence from MP3 or live mic"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onQuickCloneClick() }
                        .testTag("quick_clone_cta"),
                    color = CoralCloneSecondary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Clone Voice",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBengali) "ভয়েস ক্লোন" else "Clone Now",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceProfileCarousel(
    voices: List<ClonedVoiceEntity>,
    selectedVoiceId: Long,
    isBengali: Boolean,
    onSelectVoice: (Long) -> Unit,
    onPreviewSample: ((String, String) -> Unit)? = null,
    onDeleteClonedVoice: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_profile_carousel"),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(voices, key = { "${it.id}_${it.nameEn}" }) { voice ->
            val isSelected = voice.id == selectedVoiceId
            val accentColor = Color(voice.avatarColorHex)

            Card(
                modifier = Modifier
                    .width(215.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectVoice(voice.id) }
                    .testTag("voice_card_${voice.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        accentColor.copy(alpha = 0.16f)
                    } else {
                        StudioCardElevatedDark
                    }
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (voice.isCloned) Icons.Default.GraphicEq else Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (voice.isCloned) CoralCloneSecondary.copy(alpha = 0.22f) else accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (voice.isCloned) {
                                        if (isBengali) "ক্লোন করা" else "CLONED"
                                    } else {
                                        "${voice.fundamentalHz} Hz"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (voice.isCloned) CoralCloneSecondary else accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected Voice",
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        } else if (voice.isCloned && onDeleteClonedVoice != null) {
                            IconButton(
                                onClick = { onDeleteClonedVoice(voice.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Cloned Voice",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBengali) voice.nameBn else voice.nameEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isBengali) voice.descriptionBn else voice.descriptionEn,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.height(34.dp)
                    )

                    if (voice.isCloned && !voice.sampleAudioPath.isNullOrBlank() && onPreviewSample != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onPreviewSample(
                                        voice.sampleAudioPath,
                                        if (isBengali) voice.nameBn else voice.nameEn
                                    )
                                },
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Play Original Sample",
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBengali) "আসল কণ্ঠ শুনুন" else "Hear Original Sample",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveWaveformPlayerCard(
    title: String,
    subtitle: String,
    filePath: String,
    waveformPeaks: List<Float>,
    playbackState: PlaybackState,
    isBengali: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isThisPlaying = playbackState.currentFilePath == filePath && playbackState.isPlaying
    val progress = if (playbackState.currentFilePath == filePath) {
        playbackState.progressFraction
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("waveform_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A28)),
        border = BorderStroke(1.2.dp, CyanWavePrimary.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanWavePrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.testTag("download_audio_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = if (isBengali) "ডাউনলোড করুন" else "Download Audio",
                            tint = EmeraldSuccess
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("share_audio_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (isBengali) "শেয়ার করুন" else "Share Audio",
                            tint = CyanWavePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("play_pause_audio_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isThisPlaying) CoralCloneSecondary else CyanWavePrimary,
                        contentColor = Color(0xFF001F26)
                    )
                ) {
                    Icon(
                        imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isThisPlaying) "Pause" else "Play",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Interactive Acoustic Waveform Canvas
                val displayPeaks = if (playbackState.currentFilePath == filePath && playbackState.waveformPeaks.isNotEmpty()) {
                    playbackState.waveformPeaks
                } else {
                    waveformPeaks.ifEmpty { List(48) { 0.3f } }
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .pointerInput(filePath) {
                            detectTapGestures { offset ->
                                if (size.width > 0) {
                                    val tapFraction = (offset.x / size.width).coerceIn(0f, 1f)
                                    onSeek(tapFraction)
                                }
                            }
                        }
                ) {
                    val count = displayPeaks.size.coerceAtLeast(1)
                    val totalW = size.width
                    val totalH = size.height
                    val slotW = totalW / count
                    val barW = (slotW * 0.62f).coerceAtLeast(2.dp.toPx())

                    for (i in 0 until count) {
                        val barFraction = i.toFloat() / count
                        val amp = displayPeaks[i].coerceIn(0.10f, 1.0f)
                        val barH = (totalH * amp).coerceAtLeast(6.dp.toPx())
                        val left = i * slotW + (slotW - barW) / 2f
                        val top = (totalH - barH) / 2f

                        val barColor = if (barFraction <= progress) {
                            CyanWavePrimary
                        } else {
                            Color(0xFF3A4659)
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveRecordingVisualizer(
    amplitude: Float,
    secondsElapsed: Int,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF281317)),
        border = BorderStroke(1.5.dp, CoralCloneSecondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((12 * pulse).dp)
                        .clip(CircleShape)
                        .background(CoralCloneSecondary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                val mins = secondsElapsed / 60
                val secs = secondsElapsed % 60
                val timeFormatted = "%02d:%02d".format(mins, secs)
                Text(
                    text = if (isBengali) {
                        "লাইভ কণ্ঠ রেকর্ড হচ্ছে • $timeFormatted"
                    } else {
                        "Live Voice Recording • $timeFormatted"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CoralCloneSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val barCount = 36
                val w = size.width
                val h = size.height
                val slot = w / barCount
                val barW = slot * 0.6f
                for (i in 0 until barCount) {
                    val waveFactor = (0.35f + 0.65f * abs(sin((i * 0.45f) + pulse * 3f))).toFloat()
                    val barH = (h * (0.15f + amplitude * 0.85f * waveFactor)).coerceIn(6.dp.toPx(), h)
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(CoralCloneSecondary, CyanWavePrimary)
                        ),
                        topLeft = Offset(i * slot, (h - barH) / 2f),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                }
            }
        }
    }
}
