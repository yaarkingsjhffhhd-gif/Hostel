package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.GolaTheme
import com.example.ui.viewmodel.GolaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GolaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GolaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("login") {
                            LoginScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("home") {
                            HomeScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("text_to_video") {
                            TextToVideoScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("image_to_video") {
                            ImageToVideoScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("gallery") {
                            VideoGalleryScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("player") {
                            VideoPlayerScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("credits") {
                            CreditsSystemScreen(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
