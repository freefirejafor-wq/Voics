package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AudioLibraryScreen
import com.example.ui.screens.TtsStudioScreen
import com.example.ui.screens.VoiceChangerScreen
import com.example.ui.screens.VoiceCloneScreen
import com.example.ui.theme.CoralCloneSecondary
import com.example.ui.theme.CyanWavePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioSurfaceDark
import com.example.util.configureAppCheck
import com.example.viewmodel.StudioTab
import com.example.viewmodel.VoiceStudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                KobiVoiceStudioApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KobiVoiceStudioApp(
    studioViewModel: VoiceStudioViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by studioViewModel.uiState.collectAsStateWithLifecycle()
    val voices by studioViewModel.voices.collectAsStateWithLifecycle()
    val generatedAudios by studioViewModel.generatedAudios.collectAsStateWithLifecycle()
    val playbackState by studioViewModel.playbackState.collectAsStateWithLifecycle()
    val recordingAmplitude by studioViewModel.recordingAmplitude.collectAsStateWithLifecycle()
    val recordingSeconds by studioViewModel.recordingSeconds.collectAsStateWithLifecycle()

    var pendingRecordAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingRecordAction?.invoke()
        }
        pendingRecordAction = null
    }

    val requestMicAndRun: (() -> Unit) -> Unit = { action ->
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            action()
        } else {
            pendingRecordAction = action
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Audio File Picker for Voice Cloning (MP3 / WAV / M4A / OGG)
    val cloneAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            studioViewModel.onAudioFilePickedForCloning(uri)
        }
    }

    // Audio File Picker for Voice Changer (MP3 / WAV / M4A / OGG)
    val changerAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            studioViewModel.onAudioFilePickedForChanger(uri)
        }
    }

    // BackHandler for secondary tabs returning to primary TTS Studio tab
    if (uiState.currentTab != StudioTab.TTS_STUDIO) {
        BackHandler {
            studioViewModel.selectTab(StudioTab.TTS_STUDIO)
        }
    }

    val isBn = uiState.isBengali

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioSurfaceDark
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CyanWavePrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = CyanWavePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "কণ্ঠ স্টুডিও (KobiVoice)" else "KobiVoice Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBn) {
                                    "এআই টেক্সট-টু-ভয়েস ও ভয়েস ক্লোনিং"
                                } else {
                                    "AI Voice Cloning & TTS Studio"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { studioViewModel.toggleLanguage() }
                            .testTag("language_toggle_button"),
                        shape = RoundedCornerShape(50),
                        color = CyanWavePrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CyanWavePrimary.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = stringResource(R.string.cd_language_toggle),
                                tint = CyanWavePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "বাংলা • EN" else "EN • বাংলা",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanWavePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurfaceDark,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple(
                        StudioTab.TTS_STUDIO,
                        if (isBn) "টেক্সট টু ভয়েস" else "Text to Voice",
                        Icons.Default.RecordVoiceOver
                    ),
                    Triple(
                        StudioTab.VOICE_CLONE,
                        if (isBn) "ভয়েস ক্লোন" else "Voice Clone",
                        Icons.Default.GraphicEq
                    ),
                    Triple(
                        StudioTab.VOICE_CHANGER,
                        if (isBn) "ভয়েস চেঞ্জার" else "Voice Changer",
                        Icons.Default.Transform
                    ),
                    Triple(
                        StudioTab.AUDIO_LIBRARY,
                        if (isBn) "অডিও গ্যালারি" else "Library",
                        Icons.Default.LibraryMusic
                    )
                )

                navItems.forEach { (tab, label, icon) ->
                    val selected = uiState.currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { studioViewModel.selectTab(tab) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00222B),
                            selectedTextColor = CyanWavePrimary,
                            indicatorColor = CyanWavePrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Toast/Status Notification Banner
            AnimatedVisibility(visible = !uiState.bannerMessage.isNullOrBlank()) {
                val msg = uiState.bannerMessage.orEmpty()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("status_banner"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (uiState.isBannerError) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    } else {
                        CyanWavePrimary.copy(alpha = 0.16f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (uiState.isBannerError) MaterialTheme.colorScheme.error else CyanWavePrimary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { studioViewModel.dismissBanner() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss notification",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            when (uiState.currentTab) {
                StudioTab.TTS_STUDIO -> {
                    TtsStudioScreen(
                        uiState = uiState,
                        voices = voices,
                        playbackState = playbackState,
                        onScriptChange = studioViewModel::updateScriptText,
                        onSelectVoice = studioViewModel::selectVoice,
                        onDeleteClonedVoice = studioViewModel::deleteClonedVoice,
                        onSelectEmotion = studioViewModel::selectEmotion,
                        onPitchChange = studioViewModel::updatePitchMultiplier,
                        onSpeedChange = studioViewModel::updateSpeedMultiplier,
                        onWarmthChange = studioViewModel::updateWarmthOffset,
                        onClarityChange = studioViewModel::updateClarityOffset,
                        onResetTuning = studioViewModel::resetVoiceSliders,
                        onGenerateVoice = studioViewModel::synthesizeCurrentScript,
                        onGenerateAiScript = studioViewModel::generateAiScript,
                        onPlaySample = { path, title ->
                            studioViewModel.audioController.playAudioFile(path, title)
                        },
                        onPlayPauseLatest = {
                            val path = uiState.latestGeneratedAudioPath
                            if (!path.isNullOrBlank()) {
                                studioViewModel.audioController.playAudioFile(
                                    path,
                                    uiState.latestGeneratedTitle,
                                    uiState.latestWaveformPeaks
                                )
                            }
                        },
                        onSeekLatest = studioViewModel.audioController::seekToFraction,
                        onDownloadLatest = {
                            val path = uiState.latestGeneratedAudioPath
                            if (!path.isNullOrBlank()) {
                                studioViewModel.downloadAudioFile(path, uiState.latestGeneratedTitle)
                            }
                        },
                        onShareLatest = {
                            val path = uiState.latestGeneratedAudioPath
                            if (!path.isNullOrBlank()) {
                                studioViewModel.shareAudioFile(path, uiState.latestGeneratedTitle)
                            }
                        },
                        onNavigateToClone = {
                            studioViewModel.selectTab(StudioTab.VOICE_CLONE)
                        }
                    )
                }

                StudioTab.VOICE_CLONE -> {
                    VoiceCloneScreen(
                        isBengali = isBn,
                        cloneDraft = uiState.cloneDraft,
                        recordingAmplitude = recordingAmplitude,
                        recordingSeconds = recordingSeconds,
                        playbackState = playbackState,
                        onVoiceNameChange = studioViewModel::updateCloneVoiceName,
                        onStartRecording = {
                            requestMicAndRun { studioViewModel.startVoiceCloneRecording() }
                        },
                        onStopRecording = studioViewModel::stopVoiceCloneRecording,
                        onPickAudioFile = {
                            cloneAudioPickerLauncher.launch("audio/*")
                        },
                        onPerformVoiceClone = studioViewModel::performFullVoiceCloning,
                        onPlayPreview = { path, title ->
                            studioViewModel.audioController.playAudioFile(path, title)
                        },
                        onSeekPreview = studioViewModel.audioController::seekToFraction,
                        onDownloadPreview = studioViewModel::downloadAudioFile,
                        onSharePreview = studioViewModel::shareAudioFile,
                        onUseClonedVoiceInTts = {
                            studioViewModel.selectTab(StudioTab.TTS_STUDIO)
                        }
                    )
                }

                StudioTab.VOICE_CHANGER -> {
                    VoiceChangerScreen(
                        isBengali = isBn,
                        changerDraft = uiState.changerDraft,
                        voices = voices,
                        selectedVoiceId = uiState.selectedVoiceId,
                        recordingAmplitude = recordingAmplitude,
                        recordingSeconds = recordingSeconds,
                        playbackState = playbackState,
                        onSelectTargetVoice = studioViewModel::selectVoice,
                        onSelectMode = studioViewModel::setVoiceChangerMode,
                        onStartRecording = {
                            requestMicAndRun { studioViewModel.startVoiceChangerRecording() }
                        },
                        onStopRecording = studioViewModel::stopVoiceChangerRecording,
                        onPickAudioFile = {
                            changerAudioPickerLauncher.launch("audio/*")
                        },
                        onConvertVoice = studioViewModel::convertVoiceToSelectedTarget,
                        onPlayAudio = { path, title ->
                            studioViewModel.audioController.playAudioFile(path, title)
                        },
                        onSeekAudio = studioViewModel.audioController::seekToFraction,
                        onDownloadAudio = studioViewModel::downloadAudioFile,
                        onShareAudio = studioViewModel::shareAudioFile
                    )
                }

                StudioTab.AUDIO_LIBRARY -> {
                    AudioLibraryScreen(
                        isBengali = isBn,
                        audios = generatedAudios,
                        playbackState = playbackState,
                        onPlayAudio = { path, title ->
                            studioViewModel.audioController.playAudioFile(path, title)
                        },
                        onDownloadAudio = studioViewModel::downloadAudioFile,
                        onShareAudio = studioViewModel::shareAudioFile,
                        onDeleteAudio = studioViewModel::deleteGeneratedAudio
                    )
                }
            }
        }
    }
}
