package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.example.data.database.VideoEntity
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

// Represents a particle inside the canvas scene
data class CanvasParticle(
    var x: Float,
    var y: Float,
    var speed: Float,
    val size: Float,
    val angle: Float,
    val color: Color,
    val alpha: Float = 0.8f,
    val phase: Float = Random.nextFloat() * 20f
)

@Composable
fun GolaCanvasPlayer(
    video: VideoEntity,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    externalIsPlaying: Boolean? = null
) {
    var isPlaying by remember(video.id) { mutableStateOf(true) }
    var progress by remember(video.id) { mutableStateOf(0f) }

    // Synchronize play state if specified externally
    LaunchedEffect(externalIsPlaying) {
        if (externalIsPlaying != null) {
            isPlaying = externalIsPlaying
        }
    }

    val primaryColor = remember(video.primaryColor) {
        try { Color(android.graphics.Color.parseColor(video.primaryColor)) } catch (e: Exception) { Color(0xFF00F0FF) }
    }
    val secondaryColor = remember(video.secondaryColor) {
        try { Color(android.graphics.Color.parseColor(video.secondaryColor)) } catch (e: Exception) { Color(0xFFFF007F) }
    }

    // Animation Loop
    LaunchedEffect(isPlaying, video.id) {
        if (isPlaying) {
            val totalSteps = (video.duration * 1000) / 16
            while (progress < 1f && isPlaying) {
                delay(16)
                progress += 1f / totalSteps
                if (progress >= 1f) {
                    progress = 1f
                    isPlaying = false
                }
            }
        }
    }

    // Interactive Particle List Instantiation
    val particles = remember(video.id) {
        List(110) {
            CanvasParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.002f + Random.nextFloat() * 0.008f * (video.motionStrength / 5.0f),
                size = 6f + Random.nextFloat() * 18f,
                angle = Random.nextFloat() * 360f,
                color = if (Random.nextBoolean()) primaryColor.copy(alpha = 0.5f + Random.nextFloat() * 0.5f)
                        else secondaryColor.copy(alpha = 0.5f + Random.nextFloat() * 0.5f)
            )
        }
    }

    // Rotation angle for camera movement orbit
    val infiniteTransition = rememberInfiniteTransition(label = "camera")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isFullScreen) 0.dp else 16.dp))
            .background(Color(0xFF06060A))
    ) {
        // Core Animation Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width < 1f || height < 1f) return@Canvas

            // 1. Draw Lighting Style Backdrop
            drawBackdrop(width, height, primaryColor, secondaryColor, video.lightingStyle)

            // 2. Camera Movement adjustments
            val cameraMatrix = getCameraMatrix(width, height, video.cameraMovement, orbitAngle, bounceScale, progress)

            // Apply Camera transformations
            translate(cameraMatrix.translateX, cameraMatrix.translateY) {
                scale(cameraMatrix.scale, Offset(width / 2, height / 2)) {
                    rotate(cameraMatrix.rotation, Offset(width / 2, height / 2)) {
                        // 3. Draw scene layers based on prompt
                        drawRenderedSceneLayers(
                            width, height,
                            particles,
                            video.particlesName,
                            isPlaying,
                            progress,
                            primaryColor,
                            secondaryColor
                        )
                    }
                }
            }

            // 4. Draw Overlay vignette / Cinema shadows
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.7f))
                ),
                size = size
            )
        }

        // Playback completed banner
        if (progress >= 1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            progress = 0f
                            isPlaying = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Replay Video", tint = Color.Black)
                    }
                    Text(
                        text = "Rendering Complete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // On-screen player metadata overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                if (!isFullScreen) {
                    Text(
                        text = "AI VEO FAST RENDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = video.prompt,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Control HUD at baseline
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (progress >= 1f) {
                        progress = 0f
                        isPlaying = true
                    } else {
                        isPlaying = !isPlaying
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying && progress < 1f) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play Control",
                    tint = Color.White
                )
            }

            Text(
                text = "${(progress * video.duration).toInt()}s",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Slider(
                value = progress,
                onValueChange = {
                    progress = it
                    if (progress < 1f && !isPlaying) {
                        // Allow dragging state updates
                    }
                },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f),
                    thumbColor = MaterialTheme.colorScheme.secondary
                )
            )

            Text(
                text = "${video.duration}s",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

// Draw Background Lighting Styles
private fun DrawScope.drawBackdrop(
    width: Float,
    height: Float,
    primaryColor: Color,
    secondaryColor: Color,
    style: String
) {
    when (style) {
        "cyberpunk_neon", "neon" -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(width * 0.25f, height * 0.3f),
                    radius = width * 0.65f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(width * 0.75f, height * 0.7f),
                    radius = width * 0.65f
                )
            )
        }
        "cinematic_sunset" -> {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.4f), secondaryColor.copy(alpha = 0.2f), Color.Black),
                    start = Offset(0f, 0f),
                    end = Offset(0f, height)
                )
            )
        }
        "mystic_glow" -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.45f), secondaryColor.copy(alpha = 0.15f), Color.Black),
                    center = Offset(width / 2, height / 2),
                    radius = width * 0.8f
                )
            )
        }
        "monochrome_dramatic" -> {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Black),
                    start = Offset(0f, 0f),
                    end = Offset(width, height)
                )
            )
        }
        else -> {
            // Hyper-realiastic ambient gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.25f), secondaryColor.copy(alpha = 0.25f), Color.Black),
                    center = Offset(width * 0.45f, height * 0.5f),
                    radius = width * 0.7f
                )
            )
        }
    }
}

private fun DrawScope.drawRenderedSceneLayers(
    width: Float,
    height: Float,
    particles: List<CanvasParticle>,
    particleStyle: String,
    isPlaying: Boolean,
    progress: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    particles.forEachIndexed { i, p ->
        // Update particle offsets if playing
        if (isPlaying) {
            when (particleStyle) {
                "matrix_rain" -> {
                    p.y += p.speed * 2f
                    if (p.y > 1f) {
                        p.y = 0f
                        p.x = Random.nextFloat()
                    }
                }
                "digital_bubbles" -> {
                    p.y -= p.speed * 1.2f
                    p.x += sin(progress * 15f + p.phase) * 0.002f
                    if (p.y < 0f) {
                        p.y = 1f
                        p.x = Random.nextFloat()
                    }
                }
                "snow_drift" -> {
                    p.y += p.speed
                    p.x += p.speed * 0.5f
                    if (p.y > 1f) p.y = 0f
                    if (p.x > 1f) p.x = 0f
                }
                "fireflies" -> {
                    p.x += sin(progress * 8f + p.phase) * 0.003f
                    p.y += cos(progress * 8f + p.phase) * 0.003f
                    if (p.x < 0f || p.x > 1f) p.x = Random.nextFloat()
                    if (p.y < 0f || p.y > 1f) p.y = Random.nextFloat()
                }
                else -> { // cosmic_stars
                    p.x += (p.x - 0.5f) * p.speed * 1.5f
                    p.y += (p.y - 0.5f) * p.speed * 1.5f
                    if (p.x < 0f || p.x > 1f || p.y < 0f || p.y > 1f) {
                        p.x = 0.4f + Random.nextFloat() * 0.2f
                        p.y = 0.4f + Random.nextFloat() * 0.2f
                    }
                }
            }
        }

        val drawX = p.x * width
        val drawY = p.y * height

        // Highlight/pulsate particle sizes for visual richness
        val renderPulse = sin(progress * 25f + p.phase) * 0.25f + 1f
        val renderSize = p.size * renderPulse

        // Color modulation
        val colorAlpha = p.alpha * (1f - progress * 0.2f)

        when (particleStyle) {
            "matrix_rain" -> {
                drawRect(
                    color = primaryColor.copy(alpha = colorAlpha),
                    topLeft = Offset(drawX, drawY),
                    size = Size(renderSize * 0.3f, renderSize * 2f)
                )
            }
            "digital_bubbles" -> {
                drawCircle(
                    color = secondaryColor.copy(alpha = colorAlpha),
                    radius = renderSize / 2f,
                    center = Offset(drawX, drawY),
                    style = strokeStyle(3f)
                )
            }
            "snow_drift" -> {
                drawCircle(
                    color = Color.White.copy(alpha = colorAlpha * 0.9f),
                    radius = renderSize / 2f,
                    center = Offset(drawX, drawY)
                )
            }
            "fireflies" -> {
                val flickerAlpha = (sin(progress * 30f + p.phase) * 0.4f + 0.6f) * colorAlpha
                drawCircle(
                    color = Color(0xFFFFD600).copy(alpha = flickerAlpha),
                    radius = renderSize / 3f,
                    center = Offset(drawX, drawY)
                )
                drawCircle(
                    color = Color(0xFFFFD600).copy(alpha = flickerAlpha * 0.3f),
                    radius = renderSize,
                    center = Offset(drawX, drawY)
                )
            }
            else -> { // cosmic_stars
                drawCircle(
                    color = if (i % 2 == 0) primaryColor.copy(alpha = colorAlpha) else secondaryColor.copy(alpha = colorAlpha),
                    radius = renderSize / 3f,
                    center = Offset(drawX, drawY)
                )
            }
        }
    }
}

// Helper to define camera dynamics
data class CameraTransformation(
    val scale: Float,
    val rotation: Float,
    val translateX: Float,
    val translateY: Float
)

private fun getCameraMatrix(
    width: Float,
    height: Float,
    movement: String,
    orbitAngle: Float,
    bounceScale: Float,
    progress: Float
): CameraTransformation {
    return when (movement) {
        "orbit" -> CameraTransformation(
            scale = bounceScale,
            rotation = orbitAngle * 0.2f,
            translateX = 0f,
            translateY = 0f
        )
        "zoom_in" -> CameraTransformation(
            scale = 1f + (progress * 0.25f),
            rotation = 0f,
            translateX = 0f,
            translateY = 0f
        )
        "zoom_out" -> CameraTransformation(
            scale = 1.35f - (progress * 0.25f),
            rotation = 0f,
            translateX = 0f,
            translateY = 0f
        )
        "pan_left" -> CameraTransformation(
            scale = 1.05f,
            rotation = 0f,
            translateX = progress * (width * 0.08f),
            translateY = 0f
        )
        "pan_right" -> CameraTransformation(
            scale = 1.05f,
            rotation = 0f,
            translateX = -progress * (width * 0.08f),
            translateY = 0f
        )
        "tilt" -> CameraTransformation(
            scale = 1.05f,
            rotation = sin(progress * 3.14f) * 1.5f,
            translateX = 0f,
            translateY = progress * (height * 0.08f)
        )
        else -> CameraTransformation(1f, 0f, 0f, 0f)
    }
}

private fun strokeStyle(width: Float) = androidx.compose.ui.graphics.drawscope.Stroke(
    width = width,
    pathEffect = null
)
