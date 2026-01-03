package com.techaventus.fyp.view

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.techaventus.fyp.viewmodel.VM

@Composable
fun App(viewModel: VM = VM()) {
    val screen by viewModel.screen.collectAsState()
    val showSplash by viewModel.showSplash.collectAsState()
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }

    MaterialTheme {
        if (showSplash) {
            // Show Splash Screen
            SplashScreen(
                onSplashComplete = { viewModel.completeSplash() }
            )
        } else {
            // Main App Content
            BackHandler(enabled = true) {
                when (screen) {
                    "room" -> viewModel.leaveRoom()
                    "create" -> viewModel.setScreen("main")
                    "edit_profile" -> viewModel.setScreen("main")
                    "main" -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - backPressedTime < 2000) {
                            (context as? ComponentActivity)?.finish()
                        } else {
                            Toast.makeText(
                                context,
                                "Press back again to exit",
                                Toast.LENGTH_SHORT
                            ).show()
                            backPressedTime = currentTime
                        }
                    }

                    "auth" -> {
                        (context as? ComponentActivity)?.finish()
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (screen) {
                    "auth" -> AuthScreen(viewModel)
                    "main" -> MainScreen(viewModel)
                    "create" -> CreateRoomScreen(viewModel)
                    "edit_profile" -> EditProfileScreen(viewModel)
                    "room" -> RoomScreen(viewModel)
                }
            }
        }
    }
}