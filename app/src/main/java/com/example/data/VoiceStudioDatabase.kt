package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceStudioDao {
    @Query("SELECT * FROM voice_profiles ORDER BY isCloned DESC, id ASC")
    fun observeAllVoices(): Flow<List<ClonedVoiceEntity>>

    @Query("SELECT COUNT(*) FROM voice_profiles")
    suspend fun getVoiceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoice(voice: ClonedVoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVoices(voices: List<ClonedVoiceEntity>)

    @Query("DELETE FROM voice_profiles WHERE id = :voiceId AND isCloned = 1")
    suspend fun deleteClonedVoice(voiceId: Long)

    @Query("SELECT * FROM generated_audios ORDER BY createdAt DESC")
    fun observeGeneratedAudios(): Flow<List<GeneratedAudioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedAudio(audio: GeneratedAudioEntity): Long

    @Query("DELETE FROM generated_audios WHERE id = :audioId")
    suspend fun deleteGeneratedAudio(audioId: Long)
}

@Database(
    entities = [ClonedVoiceEntity::class, GeneratedAudioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VoiceStudioDatabase : RoomDatabase() {
    abstract fun voiceStudioDao(): VoiceStudioDao

    companion object {
        @Volatile
        private var INSTANCE: VoiceStudioDatabase? = null

        fun getInstance(context: Context): VoiceStudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoiceStudioDatabase::class.java,
                    "kobivoice_studio.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
