package com.techaventus.abc.view.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techaventus.abc.viewmodel.KosmiViewModel

@Composable
fun RoomsTab(viewModel: KosmiViewModel) {
    val rooms by viewModel.rooms.collectAsState()
    Column {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Public Rooms",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(
                    onClick = { viewModel.setScreen("create") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                ) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Create")
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms) { room ->
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
                                .background(Color(0xFFEC4899)), contentAlignment = Alignment.Center
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
                        }
                    }
                }
            }
        }
    }
}
