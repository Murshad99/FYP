package com.techaventus.abc.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techaventus.abc.viewmodel.KosmiViewModel

@Composable
fun CreateRoomScreen(viewModel: KosmiViewModel) {
    var roomName by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0F172A))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Row(modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = { viewModel.setScreen("main") }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        tint = Color.White
                    )
                }
                Text(
                    "Create Room",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                roomName,
                { roomName = it },
                label = { Text("Room Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            OutlinedTextField(
                videoUrl,
                { videoUrl = it },
                label = { Text("Video URL") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Text(
                "Supported: YouTube links or direct MP4 URLs",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(isPublic, { isPublic = it })
                Spacer(Modifier.width(8.dp))
                Text("Public Room", color = Color.White)
            }
            Button(
                onClick = { viewModel.createRoom(roomName, videoUrl, isPublic) },
                modifier = Modifier.fillMaxWidth(),
                enabled = roomName.isNotBlank() && videoUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
            ) {
                Text("Create")
            }
        }
    }
}
