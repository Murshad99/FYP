package com.techaventus.fyp.view.tabs

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
import androidx.compose.material3.ButtonDefaults
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
import com.techaventus.fyp.viewmodel.VM

@Composable
fun RoomsTab(viewModel: VM) {
    val rooms by viewModel.rooms.collectAsState()
    val myRooms by viewModel.myRooms.collectAsState()
    var selectedTab by remember { mutableStateOf("public") }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showJoinRoomDialog by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }

    // Refresh rooms when tab opens
    LaunchedEffect(Unit) {
        viewModel.fetchPublicRooms()
        viewModel.fetchMyRooms()
    }

    if (showCreateRoomDialog) {
        Dialog(onDismissRequest = { showCreateRoomDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E293B)) {
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
                        onClick = { showJoinRoomDialog = true },
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
        Dialog(onDismissRequest = { showJoinRoomDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1E293B)) {
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
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Column {
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
                            if (selectedTab == "public") "${rooms.size} public rooms" else "${myRooms.size} my rooms",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = { showCreateRoomDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedTab = "public" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "public") Color(0xFFEC4899) else Color(
                                0xFF334155
                            )
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Public")
                    }
                    Button(
                        onClick = { selectedTab = "myrooms" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "myrooms") Color(0xFFEC4899) else Color(
                                0xFF334155
                            )
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("My Rooms")
                    }
                }
            }
        }

        // Display rooms based on selected tab
        val currentUserId = viewModel.currentUserId
        val displayRooms =
            if (selectedTab == "public") rooms.filter { it.isPublic }
            else myRooms.filter { it.creatorId == currentUserId }

        if (displayRooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Videocam,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (selectedTab == "public") "No public rooms available" else "No rooms created yet",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Text(
                        if (selectedTab == "public") "Create one to get started!" else "Create your first room!",
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
                items(displayRooms) { room ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.joinRoom(room.roomId) },
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
                                Icon(Icons.Default.Videocam, null, tint = Color.White)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        room.roomName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    // Show private badge
                                    Surface(
                                        color = if (room.isPublic) Color(0xFF10B981) else Color(
                                            0xFFEF4444
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            if (room.isPublic) "Public" else "Private",
                                            modifier = Modifier.padding(
                                                horizontal = 6.dp,
                                                vertical = 2.dp
                                            ),
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                }
                                Text("by ${room.creator}", fontSize = 14.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
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

                            // Show delete button only in My Rooms tab
                            if (displayRooms.contains(room)) {
                                IconButton(onClick = { viewModel.deleteRoom(room.roomId) }) {
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