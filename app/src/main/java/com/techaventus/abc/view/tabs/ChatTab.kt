package com.techaventus.abc.view.tabs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun ChatsTab(viewModel: KosmiViewModel) {
    val friends by viewModel.friends.collectAsState()
    val requests by viewModel.friendRequests.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var searchEmail by remember { mutableStateOf("") }

    Column {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Chats", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = { showAdd = true }) {
                    Icon(
                        Icons.Default.PersonAdd,
                        null,
                        tint = Color.White
                    )
                }
            }
        }

        if (requests.isNotEmpty()) {
            Text(
                "Friend Requests",
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp,
                color = Color.White
            )
            requests.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(req.fromUsername, modifier = Modifier.weight(1f), color = Color.White)
                        IconButton(onClick = { viewModel.acceptFriendRequest(req) }) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = Color.Green
                            )
                        }
                        IconButton(onClick = { viewModel.rejectFriendRequest(req) }) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(friends) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEC4899)), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                friend.username.first().toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(friend.username, color = Color.White)
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false }, title = { Text("Add Friend") },
            text = {
                OutlinedTextField(
                    searchEmail,
                    { searchEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.searchUserByEmail(searchEmail) {
                        it?.let {
                            viewModel.sendFriendRequest(
                                it.userId
                            ); showAdd = false
                        }
                    }
                }) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } })
    }
}