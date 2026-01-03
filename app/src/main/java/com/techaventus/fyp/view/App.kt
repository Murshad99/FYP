package com.techaventus.fyp.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.techaventus.fyp.viewmodel.VM

@Composable
fun App(viewModel: VM = VM()) {
    val screen by viewModel.screen.collectAsState()
    MaterialTheme {
        when (screen) {
            "auth" -> AuthScreen(viewModel)
            "main" -> MainScreen(viewModel)
            "create" -> CreateRoomScreen(viewModel)
            "edit_profile" -> EditProfileScreen(viewModel)
            "room" -> RoomScreen(viewModel)
        }
    }
}