package com.techaventus.abc.view.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.techaventus.abc.viewmodel.VM

@Composable
fun RoomsTab(viewModel: VM) {
    val rooms by viewModel.rooms.collectAsState()
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showJoinRoomDialog by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }


    // Refresh rooms when tab opens
    LaunchedEffect(Unit) {
        viewModel.fetchPublicRooms()
    }


    if (showCreateRoomDialog) {
        Dialog(
            onDismissRequest = { showCreateRoomDialog = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Room Options",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Button(
                        onClick = {
                            showCreateRoomDialog = false
                            viewModel.setScreen("create")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Create Room")
                    }

                    Button(
                        onClick = {
                            showJoinRoomDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Join Room")
                    }
                }
            }
        }
    }



    if (showJoinRoomDialog) {
        Dialog(
            onDismissRequest = { showJoinRoomDialog = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Join Room",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL....", color = Color.White.copy(0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color.White.copy(0.3f)
                        )
                    )

                    Button(
                        onClick = {
                            val roomId = viewModel.extractRoomIdFromUrl(url)
                            showJoinRoomDialog = false
                            viewModel.joinRoom(roomId.toString())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join Room")
                    }
                }
            }
        }
    }

    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color(0xFF1E293B)
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
                        "Rooms",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${rooms.size} rooms available",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = { showCreateRoomDialog = true },
                ) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No public rooms available",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Text(
                        "Create one to get started!",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms) { room ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.joinRoom(room.roomId)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEC4899)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Videocam,
                                    null, tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    room.roomName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "by ${room.creator}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Green
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${room.members.size} watching",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            if (room.creatorId == viewModel.currentUserId) {
                                IconButton(
                                    onClick = {
                                        viewModel.deleteRoom(room.roomId)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Room",
                                        tint = Color.Red
                                    )
                                }
                            }

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                null,
                                tint = Color(0xFFEC4899)
                            )
                        }
                    }
                }
            }
        }
    }
}