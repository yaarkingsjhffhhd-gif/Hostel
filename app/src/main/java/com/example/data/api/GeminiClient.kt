package com.example.data.api

import android.util.Log
import com.example.data.database.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Calls Gemini API to generate professional video direction profile for an animation.
     */
    suspend fun generateVideoMetadata(
        prompt: String,
        duration: Int,
        aspectRatio: String,
        imageUrl: String? = null,
        motionStrength: Float = 5f
    ): VideoEntity = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty or placeholder. Using high-fidelity visual fallback generator.")
            return@withContext generateLocalFallback(prompt, duration, aspectRatio, imageUrl, motionStrength)
        }

        val systemInstruction = """
            You are Gola 3 AI Video generator director. Given a user prompt and video settings, generate a professional cinematic visual configuration for a 3D animated canvas rendering of this prompt.
            Response MUST be a JSON object containing EXACTLY:
            - cameraMovement: choice of ('orbit', 'pan_left', 'pan_right', 'zoom_in', 'zoom_out', 'tilt')
            - lightingStyle: choice of ('neon', 'cinematic_sunset', 'mystic_glow', 'cyberpunk_neon', 'monochrome_dramatic', 'hyper_realistic')
            - primaryColor: a beautiful CSS hex color string (e.g. '#00F0FF') matching the scene mood
            - secondaryColor: another hex color string (e.g. '#FF007F') that contrasts primaryColor beautifully
            - particlesName: choice of ('cosmic_stars', 'digital_bubbles', 'matrix_rain', 'snow_drift', 'fireflies')
            - expandedPrompt: a 2-sentence dramatic cinematic script description of how camera, lights, and objects behave.

            Return ONLY the valid JSON object. No markdown, no backticks, no text before or after.
        """.trimIndent()

        val promptBody = "Prompt: $prompt. Aspect Ratio: $aspectRatio. Motion Strength: $motionStrength/10."

        val jsonRequest = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", promptBody) })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "Unsuccessful API Response: Code=${response.code}, Body=$responseString")
                return@withContext generateLocalFallback(prompt, duration, aspectRatio, imageUrl, motionStrength)
            }

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Attempt to parse JSON response from LLM
            val directions = JSONObject(text.trim())

            VideoEntity(
                prompt = prompt,
                imageUrl = imageUrl,
                motionStrength = motionStrength,
                duration = duration,
                aspectRatio = aspectRatio,
                cameraMovement = directions.optString("cameraMovement", "orbit"),
                lightingStyle = directions.optString("lightingStyle", "neon"),
                primaryColor = directions.optString("primaryColor", "#02F0FF"),
                secondaryColor = directions.optString("secondaryColor", "#FF027F"),
                particlesName = directions.optString("particlesName", "cosmic_stars"),
                expandedPrompt = directions.optString("expandedPrompt", "A neon hologram of fields animating dynamically."),
                creationStatus = "SUCCESS"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed with error: ${e.message}", e)
            generateLocalFallback(prompt, duration, aspectRatio, imageUrl, motionStrength)
        }
    }

    private fun generateLocalFallback(
        prompt: String,
        duration: Int,
        aspectRatio: String,
        imageUrl: String? = null,
        motionStrength: Float = 5f
    ): VideoEntity {
        // High-fidelity fallback generation logic based on prompt keywords to provide matching theme
        val p = prompt.lowercase()
        val camera = when {
            p.contains("fly") || p.contains("space") -> "zoom_in"
            p.contains("car") || p.contains("run") -> "pan_right"
            p.contains("pan") -> "pan_left"
            p.contains("rotate") || p.contains("earth") -> "orbit"
            else -> "tilt"
        }

        val lighting = when {
            p.contains("cyber") || p.contains("neon") || p.contains("future") -> "cyberpunk_neon"
            p.contains("sunset") || p.contains("warm") || p.contains("gold") -> "cinematic_sunset"
            p.contains("magic") || p.contains("fantasy") || p.contains("glow") -> "mystic_glow"
            p.contains("dark") || p.contains("noir") -> "monochrome_dramatic"
            else -> "neon"
        }

        val primary = when {
            p.contains("cyber") || p.contains("water") || p.contains("ocean") -> "#00E5FF" // cyan
            p.contains("fire") || p.contains("sunset") || p.contains("red") -> "#FF3D00" // red/orange
            p.contains("nature") || p.contains("forest") || p.contains("green") -> "#00E676" // green
            p.contains("space") || p.contains("cosmic") || p.contains("purple") -> "#D500F9" // purple
            else -> "#FF1744" // vibrant red
        }

        val secondary = when {
            p.contains("cyber") -> "#FF1744" // magenta
            p.contains("fire") || p.contains("sunset") -> "#FFEA00" // yellow
            p.contains("green") -> "#FFD600" // gold
            else -> "#00E5FF" // cyan
        }

        val particles = when {
            p.contains("matrix") || p.contains("hologram") || p.contains("digital") -> "matrix_rain"
            p.contains("snow") || p.contains("winter") || p.contains("cold") -> "snow_drift"
            p.contains("forest") || p.contains("fireflies") || p.contains("night") -> "fireflies"
            p.contains("bubble") || p.contains("underwater") || p.contains("liquid") -> "digital_bubbles"
            else -> "cosmic_stars"
        }

        val expanded = "Simulated $aspectRatio cinematic scene of: '$prompt'. Rendered with progressive camera-$camera panning, dramatic $lighting ambient lighting, and ambient floating $particles."

        return VideoEntity(
            prompt = prompt,
            imageUrl = imageUrl,
            motionStrength = motionStrength,
            duration = duration,
            aspectRatio = aspectRatio,
            cameraMovement = camera,
            lightingStyle = lighting,
            primaryColor = primary,
            secondaryColor = secondary,
            particlesName = particles,
            expandedPrompt = expanded,
            creationStatus = "SUCCESS"
        )
    }
}
