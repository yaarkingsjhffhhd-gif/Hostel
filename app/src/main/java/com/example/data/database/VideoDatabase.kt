package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val credits: Int = 100,
    val isLoggedIn: Boolean = false,
    val lastRewardClaimed: Long = 0L
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val imageUrl: String? = null,
    val motionStrength: Float = 5.0f,
    val duration: Int = 5,
    val aspectRatio: String = "16:9",
    val timestamp: Long = System.currentTimeMillis(),
    val videoUrl: String = "",
    val cameraMovement: String = "orbit",
    val lightingStyle: String = "neon",
    val primaryColor: String = "#02F0FF",
    val secondaryColor: String = "#FF027F",
    val particlesName: String = "cosmic_stars",
    val expandedPrompt: String = "",
    val isFavorite: Boolean = false,
    val creationStatus: String = "SUCCESS", // "GENERATING", "SUCCESS", "FAILED"
    val error: String? = null
)

// --- DAO Interface ---

@Dao
interface VideoDao {
    // User Queries
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Video Queries
    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: Int): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Int)

    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>
}

// --- App Database Definition ---

@Database(entities = [UserEntity::class, VideoEntity::class], version = 1, exportSchema = false)
abstract class VideoDatabase : RoomDatabase() {
    abstract val videoDao: VideoDao

    companion object {
        @Volatile
        private var INSTANCE: VideoDatabase? = null

        fun getDatabase(context: Context): VideoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoDatabase::class.java,
                    "gola3_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
