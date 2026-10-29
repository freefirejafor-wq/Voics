package com.example.data

import kotlinx.coroutines.flow.Flow

class VoiceStudioRepository(private val dao: VoiceStudioDao) {

    val allVoices: Flow<List<ClonedVoiceEntity>> = dao.observeAllVoices()
    val generatedAudios: Flow<List<GeneratedAudioEntity>> = dao.observeGeneratedAudios()

    suspend fun ensureDefaultVoicesSeeded() {
        if (dao.getVoiceCount() == 0) {
            dao.insertAllVoices(defaultHumanVoices())
        }
    }

    suspend fun saveClonedVoice(voice: ClonedVoiceEntity): Long {
        return dao.insertVoice(voice)
    }

    suspend fun deleteClonedVoice(voiceId: Long) {
        dao.deleteClonedVoice(voiceId)
    }

    suspend fun saveGeneratedAudio(audio: GeneratedAudioEntity): Long {
        return dao.insertGeneratedAudio(audio)
    }

    suspend fun deleteGeneratedAudio(audioId: Long) {
        dao.deleteGeneratedAudio(audioId)
    }

    companion object {
        fun defaultHumanVoices(): List<ClonedVoiceEntity> = listOf(
            ClonedVoiceEntity(
                id = 1L,
                nameBn = "তানভীর (ভরাট পুরুষ কণ্ঠ)",
                nameEn = "Tanvir (Deep Narrator Male)",
                isCloned = false,
                gender = "MALE",
                languageTag = "bn-BD",
                basePitch = 0.88f,
                baseSpeed = 0.96f,
                formantShift = 0.90f,
                warmthDb = 4.5f,
                clarityDb = 3.0f,
                breathiness = 0.02f,
                fundamentalHz = 112,
                descriptionBn = "গল্প বলা, ডকুমেন্টারি ও আবৃত্তির জন্য ভরাট ও গম্ভীর পুরুষ কণ্ঠস্বর",
                descriptionEn = "Rich baritone voice ideal for documentaries, audiobooks & poetry",
                avatarColorHex = 0xFF00E5FF
            ),
            ClonedVoiceEntity(
                id = 2L,
                nameBn = "নুসরাত (মিষ্টি নারী কণ্ঠ)",
                nameEn = "Nusrat (Natural Warm Female)",
                isCloned = false,
                gender = "FEMALE",
                languageTag = "bn-BD",
                basePitch = 1.14f,
                baseSpeed = 1.0f,
                formantShift = 1.12f,
                warmthDb = 2.0f,
                clarityDb = 4.2f,
                breathiness = 0.04f,
                fundamentalHz = 218,
                descriptionBn = "প্রাণবন্ত, স্পষ্ট ও স্বাভাবিক বাঙালি নারী কণ্ঠস্বর",
                descriptionEn = "Expressive, clear and warm female voice for natural narration",
                avatarColorHex = 0xFFFF6D00
            ),
            ClonedVoiceEntity(
                id = 3L,
                nameBn = "রাফি (ইউটিউব ও রিলস ক্রিয়েটর)",
                nameEn = "Rafi (YouTube & Reels Creator)",
                isCloned = false,
                gender = "MALE",
                languageTag = "bn-BD",
                basePitch = 0.97f,
                baseSpeed = 1.08f,
                formantShift = 0.98f,
                warmthDb = 2.8f,
                clarityDb = 5.0f,
                breathiness = 0.01f,
                fundamentalHz = 134,
                descriptionBn = "ইউটিউব ভিডিও, টেক রিভিউ ও ফেসবুক রিলসের জন্য এনার্জেটিক কণ্ঠ",
                descriptionEn = "Crisp, modern energetic male voice tailored for YouTube & social videos",
                avatarColorHex = 0xFF00E676
            ),
            ClonedVoiceEntity(
                id = 4L,
                nameBn = "অদিতি (প্রফেশনাল সংবাদ পাঠিকা)",
                nameEn = "Aditi (Broadcast News Anchor)",
                isCloned = false,
                gender = "FEMALE",
                languageTag = "bn-BD",
                basePitch = 1.07f,
                baseSpeed = 1.04f,
                formantShift = 1.06f,
                warmthDb = 2.5f,
                clarityDb = 4.8f,
                breathiness = 0.0f,
                fundamentalHz = 198,
                descriptionBn = "টিভি সংবাদ, বিজ্ঞাপন ও প্রাতিষ্ঠানিক ভয়েসওভারের জন্য স্পষ্ট কণ্ঠ",
                descriptionEn = "Authoritative, studio-polished female voice for news & commercials",
                avatarColorHex = 0xFFB388FF
            ),
            ClonedVoiceEntity(
                id = 5L,
                nameBn = "আরমান (সিনেমাটিক থ্রিলার কণ্ঠ)",
                nameEn = "Arman (Cinematic Bass Voice)",
                isCloned = false,
                gender = "MALE",
                languageTag = "bn-BD",
                basePitch = 0.76f,
                baseSpeed = 0.90f,
                formantShift = 0.82f,
                warmthDb = 6.5f,
                clarityDb = 3.2f,
                breathiness = 0.03f,
                fundamentalHz = 96,
                descriptionBn = "সিনেমার ট্রেইলার, রহস্য গল্প ও মোটিভেশনাল স্পিচের জন্য গভীর কণ্ঠ",
                descriptionEn = "Ultra-deep resonant voice for movie trailers, mystery & motivation",
                avatarColorHex = 0xFFFFCA28
            ),
            ClonedVoiceEntity(
                id = 6L,
                nameBn = "জারা (ইংরেজি ও বাংলা গ্লোবাল ভয়েস)",
                nameEn = "Zara (Global Studio Voice)",
                isCloned = false,
                gender = "FEMALE",
                languageTag = "en-US",
                basePitch = 1.10f,
                baseSpeed = 1.02f,
                formantShift = 1.08f,
                warmthDb = 2.2f,
                clarityDb = 4.5f,
                breathiness = 0.03f,
                fundamentalHz = 210,
                descriptionBn = "ইংরেজি ও বাংলা পডকাস্ট এবং প্রেজেন্টেশনের জন্য আধুনিক কণ্ঠস্বর",
                descriptionEn = "Smooth international studio voice for podcasts & presentations",
                avatarColorHex = 0xFFFF4081
            )
        )
    }
}
