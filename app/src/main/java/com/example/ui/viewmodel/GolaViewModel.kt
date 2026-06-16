package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.UserEntity
import com.example.data.database.VideoDatabase
import com.example.data.database.VideoEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class GolaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = VideoDatabase.getDatabase(application)
    private val dao = db.videoDao

    // --- State Flows ---
    val activeUser: StateFlow<UserEntity?> = dao.getActiveUserFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val videos: StateFlow<List<VideoEntity>> = dao.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<VideoEntity>> = dao.getFavoriteVideos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationProgress = MutableStateFlow(0f)
    val generationProgress: StateFlow<Boolean> = _isGenerating.asStateFlow() // mapped or manual state

    private val _generationStatus = MutableStateFlow<String?>(null)
    val generationStatus: StateFlow<String?> = _generationStatus.asStateFlow()

    // --- Active Video Selection for Player ---
    private val _selectedVideo = MutableStateFlow<VideoEntity?>(null)
    val selectedVideo: StateFlow<VideoEntity?> = _selectedVideo.asStateFlow()

    fun selectVideo(video: VideoEntity?) {
        _selectedVideo.value = video
    }

    init {
        // Create a default user on launch if none is logged in to ensure a buttery experience
        viewModelScope.launch {
            val user = dao.getActiveUser()
            if (user == null) {
                // If there's no logged in user, look for any user in DB
                val defaultUser = UserEntity(
                    email = "creator@gola3.ai",
                    name = "Creative Director",
                    credits = 100,
                    isLoggedIn = true
                )
                dao.upsertUser(defaultUser)
            }
        }
    }

    // --- Auth Actions ---
    fun login(email: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Email cannot be empty")
            return
        }
        viewModelScope.launch {
            try {
                // Log out other users first
                val active = dao.getActiveUser()
                if (active != null) {
                    dao.upsertUser(active.copy(isLoggedIn = false))
                }

                val existingUser = dao.getUserByEmail(email)
                if (existingUser != null) {
                    val updated = existingUser.copy(isLoggedIn = true, name = name.ifBlank { existingUser.name })
                    dao.upsertUser(updated)
                } else {
                    val newUser = UserEntity(
                        email = email,
                        name = name.ifBlank { email.substringBefore("@") },
                        credits = 100,
                        isLoggedIn = true,
                        lastRewardClaimed = 0L
                    )
                    dao.upsertUser(newUser)
                }
                onSuccess()
            } catch (e: Exception) {
                onError("Login failed: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val active = dao.getActiveUser()
            if (active != null) {
                dao.upsertUser(active.copy(isLoggedIn = false))
            }
        }
    }

    // --- Credit Mechanics ---
    fun claimDailyReward(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = dao.getActiveUser()
            if (user == null) {
                onResult(false, "No active user logged in")
                return@launch
            }

            val now = System.currentTimeMillis()
            val diff = now - user.lastRewardClaimed
            val oneDayMs = 24 * 60 * 60 * 1000L

            if (diff >= oneDayMs) {
                val updatedUser = user.copy(
                    credits = user.credits + 20,
                    lastRewardClaimed = now
                )
                dao.upsertUser(updatedUser)
                onResult(true, "Successfully claimed 20 daily reward credits!")
            } else {
                val remainingMs = oneDayMs - diff
                val hours = remainingMs / (1000 * 60 * 60)
                val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
                onResult(false, "Daily reward already claimed! Try again in $hours hrs, $minutes mins.")
            }
        }
    }

    fun purchaseCredits(planName: String, amount: Int, cost: String, onCompleted: (String) -> Unit) {
        viewModelScope.launch {
            val user = dao.getActiveUser()
            if (user != null) {
                val updated = user.copy(credits = user.credits + amount)
                dao.upsertUser(updated)
                onCompleted("Successfully subscribed to $planName! Added $amount credits.")
            } else {
                onCompleted("Error: Login required to purchase credits.")
            }
        }
    }

    // --- Video Generation Mechanics ---
    fun generateVideoFromPrompt(
        prompt: String,
        duration: Int,
        aspectRatio: String,
        imageUrl: String? = null,
        motionStrength: Float = 5.0f,
        onSuccess: (VideoEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        if (prompt.isBlank()) {
            onError("Prompt is empty. Please describe what you want Gola to create!")
            return
        }

        viewModelScope.launch {
            val user = dao.getActiveUser()
            if (user == null) {
                onError("Please log in to generate videos.")
                return@launch
            }

            if (user.credits < 10) {
                onError("Insufficient credits. It costs 10 credits to generate an AI scene. Please claim your rewards or purchase premium credits.")
                return@launch
            }

            // Deduct credits
            dao.upsertUser(user.copy(credits = user.credits - 10))

            _isGenerating.value = true
            _generationStatus.value = "Analyzing prompt & framing camera angles..."

            try {
                // Call Gemini
                val entity = GeminiClient.generateVideoMetadata(
                    prompt = prompt,
                    duration = duration,
                    aspectRatio = aspectRatio,
                    imageUrl = imageUrl,
                    motionStrength = motionStrength
                )

                // Save to Room DB
                val insertedId = dao.insertVideo(entity)
                val savedVideo = dao.getVideoById(insertedId.toInt()) ?: entity

                _selectedVideo.value = savedVideo
                onSuccess(savedVideo)
            } catch (e: Exception) {
                onError("Generation failed: ${e.message}")
            } finally {
                _isGenerating.value = false
                _generationStatus.value = null
            }
        }
    }

    fun deleteVideo(id: Int) {
        viewModelScope.launch {
            dao.deleteVideoById(id)
        }
    }

    fun toggleFavorite(video: VideoEntity) {
        viewModelScope.launch {
            dao.updateVideo(video.copy(isFavorite = !video.isFavorite))
            // Update active selected state if needed
            if (_selectedVideo.value?.id == video.id) {
                _selectedVideo.value = video.copy(isFavorite = !video.isFavorite)
            }
        }
    }
}
