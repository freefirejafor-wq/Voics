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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.PlaybackState
import com.example.data.GeneratedAudioEntity
import com.example.data.VoiceEmotion
import com.example.ui.theme.CoralCloneSecondary
import com.example.ui.theme.CyanWavePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StudioCardElevatedDark

@Composable
fun AudioLibraryScreen(
    isBengali: Boolean,
    audios: List<GeneratedAudioEntity>,
    playbackState: PlaybackState,
    onPlayAudio: (String, String) -> Unit,
    onDownloadAudio: (String, String) -> Unit,
    onShareAudio: (String, String) -> Unit,
    onDeleteAudio: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (audios.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp)
                .testTag("audio_library_empty"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                tint = CyanWavePrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(68.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isBengali) "আপনার অডিও গ্যালারি এখনো খালি" else "Your Audio Studio Library is Empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBengali) {
                    "টেক্সট-টু-ভয়েস বা ভয়েস চেঞ্জার ট্যাব থেকে ভয়েস তৈরি করলে সেগুলো এখানে সংরক্ষিত থাকবে।"
                } else {
                    "Generate voiceovers or convert voices and they will automatically appear here for replay & export."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("audio_library_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isBengali) {
                    "সংরক্ষিত ভয়েসওভার ও অডিও (${audios.size})"
                } else {
                    "Saved Voiceovers & Conversions (${audios.size})"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(audios, key = { it.id }) { item ->
            val isPlaying = playbackState.currentFilePath == item.audioFilePath && playbackState.isPlaying
            val emotion = VoiceEmotion.fromKey(item.emotionKey)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_audio_card_${item.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevatedDark),
                border = BorderStroke(
                    1.dp,
                    if (isPlaying) CyanWavePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { onPlayAudio(item.audioFilePath, item.title) },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isPlaying) CoralCloneSecondary else CyanWavePrimary,
                                contentColor = Color(0xFF001F26)
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = CyanWavePrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isBengali) item.voiceNameBn else item.voiceNameEn,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyanWavePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${emotion.emoji} ${if (isBengali) emotion.labelBn else emotion.labelEn}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onDownloadAudio(item.audioFilePath, item.title) }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = EmeraldSuccess
                            )
                        }
                        IconButton(onClick = { onShareAudio(item.audioFilePath, item.title) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = CyanWavePrimary
                            )
                        }
                        IconButton(onClick = { onDeleteAudio(item.id) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.scriptText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
