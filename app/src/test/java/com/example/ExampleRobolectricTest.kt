package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AcousticVoiceDsp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `launch MainActivity and verify UI renders without crashing`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tts_studio_screen").assertExists()
        composeTestRule.onNodeWithTag("studio_hero_banner").assertExists()
        composeTestRule.onNodeWithTag("language_toggle_button").assertExists()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KobiVoice Studio", appName)
    }

    @Test
    fun `extract vocal dna and master wav file accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sr = 24000
        val testToneHz = 180.0
        val samples = ShortArray(sr) // 1 second of 180 Hz tone
        for (i in samples.indices) {
            val t = i.toDouble() / sr
            samples[i] = (sin(2.0 * PI * testToneHz * t) * 16000).toInt().toShort()
        }

        val inputWav = File(context.cacheDir, "test_input_180hz.wav")
        val outputWav = File(context.cacheDir, "test_output_mastered.wav")
        AcousticVoiceDsp.writeMono16BitWavFile(samples, sr, inputWav)

        val dna = AcousticVoiceDsp.extractVocalDnaFromFile(inputWav)
        assertTrue("Detected F0 should be near 180 Hz, got ${dna.fundamentalHz}", dna.fundamentalHz in 165..195)
        assertEquals(48, dna.waveformPeaks.size)

        val peaks = AcousticVoiceDsp.processAndMasterWavFile(
            inputFile = inputWav,
            outputFile = outputWav,
            pitchFactor = 1.05f,
            speedFactor = 1.0f,
            formantShift = 1.1f,
            warmthDb = 3.0f,
            clarityDb = 4.0f,
            breathiness = 0.02f
        )
        assertTrue(outputWav.exists() && outputWav.length() > 1000L)
        assertEquals(48, peaks.size)
    }
}
