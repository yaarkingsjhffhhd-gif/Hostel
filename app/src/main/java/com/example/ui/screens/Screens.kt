package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.database.VideoEntity
import com.example.ui.components.GolaCanvasPlayer
import com.example.ui.theme.*
import com.example.ui.viewmodel.GolaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Custom Preset Majestic Images for Image-to-Video
val PRESET_IMAGES = listOf(
    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=60", // Abstract Nebula Flow
    "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop&q=60", // Cosmic Starry Painting
    "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=500&auto=format&fit=crop&q=60", // Moody Dark Mountain Range
    "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500&auto=format&fit=crop&q=60", // Floating Cyber Liquid
    "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&auto=format&fit=crop&q=60"  // Cyberpunk City Alley
)

// List of Sample/Trending prompts for quick inserts
val TRENDING_PROMPTS = listOf(
    "Hyper-loop train speeding through a neon digital tunnel",
    "An astronaut ice-skating on the vibrant golden rings of Saturn",
    "Magical fantasy valley with glowing mushrooms and blue fireflies",
    "Abstract slow-motion liquid metal explosion with deep violet particles",
    "A majestic ancient dragon breathing turquoise flames in a starry sky"
)

// --- 1. SPLASH SCREEN ---
@Composable
fun SplashScreen(navController: NavController, viewModel: GolaViewModel) {
    val activeUser by viewModel.activeUser.collectAsState()

    LaunchedEffect(Unit) {
        delay(2200) // Beautiful cinematic duration
        if (activeUser != null) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Neon Logo Header Box
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.radialGradient(colors = listOf(GolaCyan, GolaMagenta))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MovieCreation,
                    contentDescription = "Gola 3 Icon",
                    tint = Color.Black,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "GOLA 3",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                ),
                color = TextPrimary
            )

            Text(
                text = "VEO AI VIDEO ENGINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                ),
                color = GolaCyan
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = GolaMagenta,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

// --- 2. LOGIN & SIGNUP SCREEN ---
@Composable
fun LoginScreen(navController: NavController, viewModel: GolaViewModel) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MovieCreation,
                contentDescription = "Gola Logo",
                tint = GolaCyan,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isSignUp) "Create Creator Account" else "Welcome to Gola 3",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Text(
                text = if (isSignUp) "Sign up to start generating premium AI scenes" else "Access your cinematic multi-scene drafts",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.login(
                                email = email,
                                name = name,
                                onSuccess = {
                                    Toast.makeText(context, "Session active: $email", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GolaCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isSignUp) "Register Draft Account" else "Start Creator Session",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Login simulation button
            Button(
                onClick = {
                    viewModel.login(
                        email = "google_user@gola3.ai",
                        name = "Google Creator",
                        onSuccess = {
                            Toast.makeText(context, "Logged in via Google Identity", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        },
                        onError = {}
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AlternateEmail,
                        contentDescription = "Google Icon",
                        tint = GolaMagenta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Connect with Google Hub")
                }
            }

            TextButton(
                onClick = { isSignUp = !isSignUp },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = if (isSignUp) "Already a creator? Login" else "New creator? Joint the Gola 3 Sandbox",
                    color = GolaCyan
                )
            }

            TextButton(
                onClick = {
                    Toast.makeText(context, "Credentials recovery instructions sent to your email registry.", Toast.LENGTH_LONG).show()
                }
            ) {
                Text("Forgot Password?", color = TextMuted)
            }
        }
    }
}

// --- 3. HOME SCREEN ---
@Composable
fun HomeScreen(navController: NavController, viewModel: GolaViewModel) {
    val user by viewModel.activeUser.collectAsState()
    val creations by viewModel.videos.collectAsState()
    var directPrompt by remember { mutableStateOf("") }
    val isGenerating by viewModel.isGenerating.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        bottomBar = { GolaBottomBar(navController, "home") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Dashboard Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${user?.name ?: "Creator"}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "What will your Gola create today?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Card(
                    onClick = { navController.navigate("credits") },
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Token, contentDescription = "Credits", tint = GolaCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user?.credits ?: 0}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Prompt Input Box Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GolaCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI VEO TEXT PROMPT ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = GolaCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = directPrompt,
                        onValueChange = { directPrompt = it },
                        placeholder = { Text("Describe a scene (e.g. A futuristic robot riding a hoverboard)...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick randomizer
                        Button(
                            onClick = {
                                directPrompt = TRENDING_PROMPTS.random()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Casino, "Surprise Me", tint = GolaCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Surprise", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                if (isGenerating) return@Button
                                viewModel.generateVideoFromPrompt(
                                    prompt = directPrompt,
                                    duration = 5,
                                    aspectRatio = "16:9",
                                    onSuccess = { video ->
                                        Toast.makeText(context, "Draft Render Completed", Toast.LENGTH_SHORT).show()
                                        navController.navigate("player")
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GolaCyan, contentColor = Color.Black),
                            modifier = Modifier.testTag("generate_button"),
                            enabled = !isGenerating && directPrompt.isNotBlank()
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Bolt, "Generate", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quick Render", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Selection shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    onClick = { navController.navigate("text_to_video") },
                    modifier = Modifier.weight(1f).border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GolaGridPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PostAdd, null, tint = GolaGridPurple, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Text-to-Video", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Detailed prompt config", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }

                Card(
                    onClick = { navController.navigate("image_to_video") },
                    modifier = Modifier.weight(1f).border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GolaMagenta.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Image, null, tint = GolaMagenta, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Image-to-Video", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Animate existing artwork", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trending prompts horizontal scroll
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Pro Prompts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(TRENDING_PROMPTS) { item ->
                    Card(
                        modifier = Modifier
                            .width(240.dp)
                            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        onClick = {
                            directPrompt = item
                        }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            iconTag(text = "CREATIVE VEO")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                maxLines = 3,
                                minLines = 3
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Add, null, tint = GolaCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Insert Prompt", style = MaterialTheme.typography.labelSmall, color = GolaCyan)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent creations
            Text(
                text = "Recent Creations",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            if (creations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.HistoryToggleOff, null, tint = TextMuted, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No creations recorded yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Generate your initial AI clip above!", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    creations.take(6).forEach { video ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            onClick = {
                                viewModel.selectVideo(video)
                                navController.navigate("player")
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Live Preview icon / thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (video.imageUrl != null) {
                                        AsyncImage(
                                            model = video.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Filled.Videocam, null, tint = GolaCyan, modifier = Modifier.size(24.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = video.prompt,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${video.aspectRatio} • ${video.duration}s • camera: ${video.cameraMovement}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = video.expandedPrompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                }

                                IconButton(onClick = { viewModel.toggleFavorite(video) }) {
                                    Icon(
                                        imageVector = if (video.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (video.isFavorite) GolaCyan else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper badge tag
@Composable
fun iconTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(GolaCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = GolaCyan
        )
    }
}

// --- 4. TEXT-TO-VIDEO SCREEN ---
@Composable
fun TextToVideoScreen(navController: NavController, viewModel: GolaViewModel) {
    var prompt by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(5) }
    var selectedRatio by remember { mutableStateOf("16:9") }
    val isGenerating by viewModel.isGenerating.collectAsState()

    val context = LocalContext.current

    Scaffold(
        bottomBar = { GolaBottomBar(navController, "create") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Text-to-Video Studio",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Turn your description into high-fidelity custom animations",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Area
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Cinematic Prompt Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("prompt_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Duration selector
            Text("Select Duration (costs 10 credits)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15).forEach { sec ->
                    FilterChip(
                        selected = selectedDuration == sec,
                        onClick = { selectedDuration = sec },
                        label = { Text("$sec Seconds") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GolaCyan,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aspect Ratio selectors
            Text("Select Aspect Ratio", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("16:9", "9:16", "1:1").forEach { ratio ->
                    FilterChip(
                        selected = selectedRatio == ratio,
                        onClick = { selectedRatio = ratio },
                        label = { Text(ratio) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GolaMagenta,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.generateVideoFromPrompt(
                        prompt = prompt,
                        duration = selectedDuration,
                        aspectRatio = selectedRatio,
                        onSuccess = {
                            Toast.makeText(context, "Rendering successful!", Toast.LENGTH_SHORT).show()
                            navController.navigate("player")
                        },
                        onError = {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GolaCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating && prompt.isNotBlank()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MovieCreation, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Draft VEO Scene (10 Cr)", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            if (isGenerating) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = GolaCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analyzing particles, rendering multi-frame vectors...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

// --- 5. IMAGE-TO-VIDEO SCREEN ---
@Composable
fun ImageToVideoScreen(navController: NavController, viewModel: GolaViewModel) {
    var prompt by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf(PRESET_IMAGES[0]) }
    var motionStrength by remember { mutableStateOf(5.0f) }
    val isGenerating by viewModel.isGenerating.collectAsState()

    val context = LocalContext.current

    Scaffold(
        bottomBar = { GolaBottomBar(navController, "create") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Image-to-Video Engine",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Fuse a dynamic visual canvas, motion vectors, and prompts",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preset visual picker
            Text("Choose Base Keyframe Image", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PRESET_IMAGES) { img ->
                    val isSelected = selectedImage == img
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { selectedImage = img }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) GolaCyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        AsyncImage(
                            model = img,
                            contentDescription = "Presets",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional movement prompt
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Movement / Direction Description (e.g. make the waves cycle)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Slider for movement speed
            Text("Motion Strength Slider: ${motionStrength.toInt()}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Slider(
                value = motionStrength,
                onValueChange = { motionStrength = it },
                valueRange = 1.0f..10.0f,
                colors = SliderDefaults.colors(
                    activeTrackColor = GolaCyan,
                    inactiveTrackColor = Color.Gray,
                    thumbColor = GolaMagenta
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalPrompt = "Motion animate preset picture: " + prompt.ifBlank { "Fluid motion dynamics on visual plane." }
                    viewModel.generateVideoFromPrompt(
                        prompt = finalPrompt,
                        duration = 10,
                        aspectRatio = "16:9",
                        imageUrl = selectedImage,
                        motionStrength = motionStrength,
                        onSuccess = {
                            Toast.makeText(context, "Motion animation active", Toast.LENGTH_SHORT).show()
                            navController.navigate("player")
                        },
                        onError = {
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GolaMagenta, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.VideoLabel, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Animate Keyframe (10 Cr)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 6. VIDEO GALLERY SCREEN ---
@Composable
fun VideoGalleryScreen(navController: NavController, viewModel: GolaViewModel) {
    val videos by viewModel.videos.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }

    val filteredList = remember(videos, favorites, searchQuery, selectedTab) {
        val base = if (selectedTab == "Favorites") favorites else videos
        base.filter {
            it.prompt.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = { GolaBottomBar(navController, "gallery") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Gola Draft Hub",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search creations by prompt keywords...") },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter tab chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Favorites").forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GolaCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.VideoSettings, null, tint = TextMuted, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching creations", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { video ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectVideo(video)
                                    navController.navigate("player")
                                }
                                .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp))
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (video.imageUrl != null) {
                                        AsyncImage(
                                            model = video.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Filled.PlayCircle, "Clip", tint = GolaCyan, modifier = Modifier.size(36.dp))
                                    }

                                    // Duration Overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("${video.duration}s", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }

                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = video.prompt,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary,
                                        maxLines = 2,
                                        minLines = 2
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { viewModel.toggleFavorite(video) }, modifier = Modifier.size(24.dp)) {
                                            Icon(
                                                imageVector = if (video.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                contentDescription = "Fav",
                                                tint = if (video.isFavorite) GolaCyan else TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(onClick = { viewModel.deleteVideo(video.id) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Filled.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 7. VIDEO PLAYER SCREEN ---
@Composable
fun VideoPlayerScreen(navController: NavController, viewModel: GolaViewModel) {
    val selectedVideo by viewModel.selectedVideo.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSimulatingDownload by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }

    Scaffold(
        bottomBar = { GolaBottomBar(navController, "gallery") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text("Scene Analyzer Player", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            val video = selectedVideo
            if (video == null) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("No video selected", color = TextSecondary)
                }
            } else {
                // Interactive Vector Canvas Player Custom View
                GolaCanvasPlayer(
                    video = video,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                // Render specifications summary details card
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = video.prompt,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconTag("Aspect: ${video.aspectRatio}")
                        iconTag("Time: ${video.duration}s")
                        iconTag("Style: ${video.lightingStyle}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "VEO Scene AI Directions",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = GolaCyan
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = video.expandedPrompt.ifBlank { "Scene directions loading..." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                specBullet("Camera Movement", video.cameraMovement)
                                specBullet("Primary Accent Theme", video.primaryColor)
                                specBullet("Secondary Accent Theme", video.secondaryColor)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSimulatingDownload) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Downloading cinematic video mp4 locally: ${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                color = GolaCyan,
                                trackColor = Color.Gray
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    isSimulatingDownload = true
                                    downloadProgress = 0f
                                    coroutineScope.launch {
                                        while (downloadProgress < 1f) {
                                            delay(150)
                                            downloadProgress += 0.1f
                                        }
                                        isSimulatingDownload = false
                                        Toast.makeText(context, "Movie saved successfully to downloads folder!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Download, null, tint = GolaCyan)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download MP4", color = Color.White)
                                }
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Gola 3 invite URL shared successfully with friends!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GolaCyan, contentColor = Color.Black)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Share, null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Link", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun specBullet(label: String, valText: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(valText, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
    }
}

// --- 8. PROFILE SCREEN ---
@Composable
fun ProfileScreen(navController: NavController, viewModel: GolaViewModel) {
    val user by viewModel.activeUser.collectAsState()
    val context = LocalContext.current

    var multiLanguageSimEnabled by remember { mutableStateOf("English (US)") }
    var pushNotificationState by remember { mutableStateOf(true) }
    var offlineCacheState by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = { GolaBottomBar(navController, "profile") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Draft Profile",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // User Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(colors = listOf(GolaCyan, GolaMagenta))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.name ?: "C").take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user?.name ?: "Gola Creator",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = user?.email ?: "creator@gola3.ai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Credits Details Shortcut click
            Card(
                onClick = { navController.navigate("credits") },
                colors = CardDefaults.cardColors(containerColor = GolaGridPurple.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, GolaGridPurple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WorkspacePremium, "Credits", tint = GolaGridPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active Token Credit Balance", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Text("${user?.credits ?: 0} Credits", fontWeight = FontWeight.ExtraBold, color = GolaCyan, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sandbox Settings Lists
            Text("Sandbox Engine Settings", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // 1. Language Selector Dialog Simulation
                    ListItem(
                        headlineContent = { Text("App Language") },
                        supportingContent = { Text(multiLanguageSimEnabled) },
                        leadingContent = { Icon(Icons.Filled.Language, null, tint = GolaCyan) },
                        trailingContent = {
                            IconButton(onClick = {
                                multiLanguageSimEnabled = if (multiLanguageSimEnabled == "English (US)") "Español (ES)" else "English (US)"
                                Toast.makeText(context, "Language switched to $multiLanguageSimEnabled", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Filled.SwapHoriz, "Swap Language")
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = BorderColor)

                    // 2. Push Notifications
                    ListItem(
                        headlineContent = { Text("Push Notification Hub") },
                        supportingContent = { Text("Receive active video render completed alerts") },
                        leadingContent = { Icon(Icons.Filled.NotificationsActive, null, tint = GolaMagenta) },
                        trailingContent = {
                            Switch(
                                checked = pushNotificationState,
                                onCheckedChange = { pushNotificationState = it }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(color = BorderColor)

                    // 3. Offline Cache control
                    ListItem(
                        headlineContent = { Text("Room Database Cache") },
                        supportingContent = { Text("Enable ultra-fast local sandbox loading logs") },
                        leadingContent = { Icon(Icons.Filled.Storage, null, tint = GolaOrange) },
                        trailingContent = {
                            Switch(
                                checked = offlineCacheState,
                                onCheckedChange = { offlineCacheState = it }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exit Session / Logout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 9. CREDITS SYSTEM SCREEN ---
@Composable
fun CreditsSystemScreen(navController: NavController, viewModel: GolaViewModel) {
    val user by viewModel.activeUser.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { GolaBottomBar(navController, "credits") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gola Credits Studio",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Powering multi-scene VEO rendering layers",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Prominent Current balance ring widget
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(GolaCyan.copy(alpha = 0.2f), Color.Transparent)))
                    .border(BorderStroke(4.dp, Brush.sweepGradient(colors = listOf(GolaCyan, GolaMagenta, GolaCyan))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${user?.credits ?: 0}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp, fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text("Credits", style = MaterialTheme.typography.bodyMedium, color = GolaCyan)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Claim daily rewards button
            Button(
                onClick = {
                    viewModel.claimDailyReward { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillPrimaryKey()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GolaCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CardGiftcard, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Claim Daily Sandbox Bonus (+20 Cr)", fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Visual plan products
            Text(
                "Premium Token Packages",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                creditPlanCard(
                    title = "Starter Pass",
                    desc = "Great for initial experiment testing drafts",
                    credits = 50,
                    price = "$4.99",
                    color = GolaCyan,
                    onBuy = {
                        viewModel.purchaseCredits("Starter Pass", 50, "$4.99") {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                creditPlanCard(
                    title = "Pro Director",
                    desc = "Superb high-frequency prompt modeling",
                    credits = 150,
                    price = "$9.99",
                    color = GolaMagenta,
                    onBuy = {
                        viewModel.purchaseCredits("Pro Director", 150, "$9.99") {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                creditPlanCard(
                    title = "Unlimited Studio",
                    desc = "Highest performance canvas vector generation",
                    credits = 500,
                    price = "$24.99",
                    color = GolaGridPurple,
                    onBuy = {
                        viewModel.purchaseCredits("Unlimited Studio", 500, "$24.99") {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun creditPlanCard(
    title: String,
    desc: String,
    credits: Int,
    price: String,
    color: Color,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Token, null, tint = color, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adds $credits visual credits", style = MaterialTheme.typography.labelSmall, color = color)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if(color == GolaCyan) Color.Black else Color.White)
            ) {
                Text("Get $price", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SHARED NAVIGATION DRAW BOTTOM BAR ---
@Composable
fun GolaBottomBar(navController: NavController, currentScreen: String) {
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "home",
            onClick = { navController.navigate("home") { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Home, "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GolaCyan,
                selectedTextColor = GolaCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = currentScreen == "create",
            onClick = { navController.navigate("text_to_video") { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Movie, "Create") },
            label = { Text("Studio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GolaCyan,
                selectedTextColor = GolaCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = currentScreen == "gallery",
            onClick = { navController.navigate("gallery") { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.PhotoLibrary, "Gallery") },
            label = { Text("Gallery") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GolaCyan,
                selectedTextColor = GolaCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = currentScreen == "credits",
            onClick = { navController.navigate("credits") { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Token, "Credits") },
            label = { Text("Billing") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GolaCyan,
                selectedTextColor = GolaCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )

        NavigationBarItem(
            selected = currentScreen == "profile",
            onClick = { navController.navigate("profile") { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Person, "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GolaCyan,
                selectedTextColor = GolaCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            )
        )
    }
}

// Utility extension for filling primary columns
fun Modifier.fillPrimaryKey(): Modifier = this.fillMaxWidth()
