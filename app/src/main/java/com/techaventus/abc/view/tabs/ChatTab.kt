package com.techaventus.abc.view.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techaventus.abc.model.Friend
import com.techaventus.abc.model.UserProfile
import com.techaventus.abc.viewmodel.VM

@Composable
fun ChatsTab(viewModel: VM) {
    val friends by viewModel.friends.collectAsState()
    val requests by viewModel.friendRequests.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var chatWithFriend by remember { mutableStateOf<Friend?>(null) }

    // Screen switch between Friend List & Chat
    if (chatWithFriend != null) {
        FriendChatScreen(viewModel, chatWithFriend!!) {
            viewModel.leaveFriendChat() // Clean up when leaving
            chatWithFriend = null
        }
        return
    }

    Column {
        // Top bar
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Chats", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                }
            }
        }

        // Friend Requests
        if (requests.isNotEmpty()) {
            Text("Friend Requests", modifier = Modifier.padding(16.dp), color = Color.White)
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
                            Text("✔", color = Color.Green)
                        }
                        IconButton(onClick = { viewModel.rejectFriendRequest(req) }) {
                            Text("✖", color = Color.Red)
                        }
                    }
                }
            }
        }

        // Friend List
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(friends) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable {
                                viewModel.joinFriendChat(friend)
                                chatWithFriend = friend
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEC4899)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                friend.username.firstOrNull()?.toString() ?: "?",
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

    // Add Friend Dialog
    if (showAdd) {
        var username by remember { mutableStateOf("") }
        var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
        var searched by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAdd = false; results = emptyList(); searched = false },
            title = { Text("Add Friend") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; searched = false; results = emptyList() },
                        label = { Text("Search by username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        searched = true
                        viewModel.searchUserByUserName(username) { results = it }
                    }) {
                        Text("Search")
                    }

                    when {
                        results.isNotEmpty() -> {
                            results.forEach { user ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFF1E293B
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            user.username,
                                            modifier = Modifier.weight(1f),
                                            color = Color.White
                                        )
                                        Button(onClick = {
                                            viewModel.sendFriendRequest(user.userId)
                                            showAdd = false
                                        }) { Text("Send") }
                                    }
                                }
                            }
                        }

                        searched -> Text("User not found", color = Color.Red, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("Close") }
            }
        )
    }
}

// Friend Chat Screen
@Composable
fun FriendChatScreen(viewModel: VM, friend: Friend, onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    val friendChatMessages by viewModel.friendChatMessages.collectAsState()
    val currentUserId = viewModel.currentUserId

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        // Top bar
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        friend.username,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friendChatMessages.reversed()) { msg ->
                val isMe = msg.senderId == currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) Color(0xFFEC4899) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isMe) {
                                Text(
                                    msg.senderName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEC4899)
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(
                                msg.message,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Message Input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E293B)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (message.isNotBlank()) {
                                viewModel.sendFriendChatMessage(message)
                                message = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFEC4899),
                        unfocusedBorderColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (message.isNotBlank()) {
                            viewModel.sendFriendChatMessage(message)
                            message = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color(0xFFEC4899), modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}