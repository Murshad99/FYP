package com.techaventus.fyp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.techaventus.fyp.viewmodel.VM

@Composable
fun CreateRoomScreen(viewModel: VM) {
    var roomName by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0F172A))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                IconButton(onClick = { viewModel.setScreen("main") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isPublic) "Public Room" else "Private Room",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            if (isPublic) "Everyone can see and join" else "Only you can see",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEC4899),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isPublic) Color(0xFF10B981).copy(0.2f) else Color(0xFFEF4444).copy(0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                        null,
                        tint = if (isPublic) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isPublic) "This room will appear in Public Rooms" else "This room will only appear in My Rooms",
                        fontSize = 12.sp,
                        color = if (isPublic) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.createRoom(roomName, isPublic)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = roomName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
            ) {
                Text("Create Room")
            }
        }
    }
}