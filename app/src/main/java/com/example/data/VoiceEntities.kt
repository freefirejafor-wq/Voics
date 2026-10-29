package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_profiles")
data class ClonedVoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameBn: String,
    val nameEn: String,
    val isCloned: Boolean,
    val gender: String, // "MALE", "FEMALE", "NEUTRAL"
    val languageTag: String, // "bn-BD", "en-US"
    val basePitch: Float, // 0.60f .. 1.50f
    val baseSpeed: Float, // 0.65f .. 1.45f
    val formantShift: Float, // 0.75f .. 1.35f (throat depth / vocal tract resonance)
    val warmthDb: Float, // -6.0f .. +9.0f (low-shelf bass warmth)
    val clarityDb: Float, // -4.0f .. +9.0f (high-shelf studio presence)
    val breathiness: Float, // 0.0f .. 0.45f (subtle harmonic air)
    val fundamentalHz: Int, // Measured F0 in Hz (e.g., 115 Hz male, 215 Hz female)
    val sampleAudioPath: String? = null,
    val descriptionBn: String,
    val descriptionEn: String,
    val avatarColorHex: Long = 0xFF00E5FF,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generated_audios")
data class GeneratedAudioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scriptText: String,
    val voiceNameBn: String,
    val voiceNameEn: String,
    val emotionKey: String,
    val audioFilePath: String,
    val durationMs: Long,
    val isVoiceChanged: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class VoiceEmotion(
    val key: String,
    val labelBn: String,
    val labelEn: String,
    val emoji: String,
    val pitchMultiplier: Float,
    val speedMultiplier: Float,
    val warmthOffsetDb: Float,
    val clarityOffsetDb: Float,
    val breathinessOffset: Float
) {
    NATURAL("NATURAL", "স্বাভাবিক (Natural)", "Natural", "🎙️", 1.0f, 1.0f, 0f, 1.5f, 0f),
    CHEERFUL("CHEERFUL", "আনন্দিত ও প্রাণবন্ত", "Cheerful & Bright", "😊", 1.08f, 1.07f, -1.0f, 3.5f, 0.02f),
    DEEP_SERIOUS("DEEP_SERIOUS", "গম্ভীর ও ভরাট", "Deep & Cinematic", "🎬", 0.90f, 0.93f, 4.5f, 1.0f, 0f),
    NEWS_ANCHOR("NEWS_ANCHOR", "সংবাদ পাঠ (News)", "News Broadcast", "📺", 1.02f, 1.05f, 1.5f, 4.5f, 0f),
    STORYTELLER("STORYTELLER", "গল্পকার (Storyteller)", "Warm Storyteller", "📖", 0.96f, 0.92f, 3.0f, 2.5f, 0.04f),
    EMOTIONAL("EMOTIONAL", "আবেগঘন (Emotional)", "Soft & Emotional", "🥺", 0.95f, 0.86f, 2.0f, -1.0f, 0.08f),
    WHISPER("WHISPER", "ফিসফিসানি (Whisper)", "Intimate Whisper", "🤫", 1.04f, 0.88f, -4.0f, 5.0f, 0.22f),
    ENERGETIC_YT("ENERGETIC_YT", "ইউটিউব/টিকটক হাইপ", "YouTube / Promo Hype", "🔥", 1.06f, 1.12f, 2.5f, 5.0f, 0f);

    companion object {
        fun fromKey(key: String): VoiceEmotion =
            entries.find { it.key == key } ?: NATURAL
    }
}
