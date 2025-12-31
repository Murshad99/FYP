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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.techaventus.abc.viewmodel.KosmiViewModel

// RoomsTab function ko yeh se replace karo
@Composable
fun RoomsTab(viewModel: KosmiViewModel) {
    val rooms by viewModel.rooms.collectAsState()
    val myRooms by viewModel.myRooms.collectAsState()
    var showMyRooms by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (showMyRooms) "My Rooms" else "Public Rooms",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showMyRooms =! showMyRooms },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showMyRooms) Color(0xFFEC4899) else Color(
                                    0xFF475569
                                )
                            )
                        ) {
                            Icon(Icons.Default.Person, null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (showMyRooms) "Public" else "Mine")
                        }
                        Button(
                            onClick = { viewModel.setScreen("create") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Create")
                        }
                    }
                }
            }
        }

        val displayRooms = if (showMyRooms) myRooms else rooms

        if (displayRooms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Videocam,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (showMyRooms) "No rooms created yet" else "No public rooms available",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.setScreen("create") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Text("Create a Room")
                    }
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
                                Text(
                                    room.roomName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text("by ${room.creator}", fontSize = 14.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = Color.Green,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${room.members.size} watching",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            if (showMyRooms) {
                                IconButton(onClick = { viewModel.deleteRoom(room.roomId) }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}