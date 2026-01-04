@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.techaventus.fyp.view

import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.techaventus.fyp.viewmodel.VM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun RoomScreen(viewModel: VM) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val room by viewModel.currentRoom.collectAsState()
    val messages by viewModel.roomMessages.collectAsState()
    val user by viewModel.user.collectAsState()
    var isPlayerReady by remember { mutableStateOf(false) }
    var currentYoutubeTime by remember { mutableStateOf(0f) }
    var messageText by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(true) }
    var showMediaSelector by remember { mutableStateOf(false) }
    var showYouTubeSearch by remember { mutableStateOf(false) }


    DisposableEffect(room?.videoUrl, room?.videoType) {
        onDispose {
            println("DEBUG: Cleaning up old video player")
            viewModel.exoPlayer?.stop()
            viewModel.exoPlayer?.release()
            viewModel.exoPlayer = null
            viewModel.youtubePlayer = null
            viewModel.isYoutubePlayerReady = false
        }
    }

    // Extract YouTube video ID
    fun getYouTubeVideoId(url: String): String? {
        val patterns = listOf(
            "(?<=watch\\?v=)[^#&?]*".toRegex(),
            "(?<=youtu.be/)[^#&?]*".toRegex(),
            "(?<=embed/)[^#&?]*".toRegex(),
            "(?<=shorts/)[^#&?]*".toRegex()
        )
        patterns.forEach { pattern ->
            pattern.find(url)?.value?.let { return it }
        }
        return null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = { Text("Room") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
                titleContentColor = Color.White
            ),
            actions = {
                IconButton(onClick = {
                    room?.let { currentRoom ->
                        val link = viewModel.getJoinRoomLink(currentRoom.roomId)

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT, "Join my room 👇\n$link"
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Room"))
                    }
                }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = { viewModel.leaveRoom() }){
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        null,
                        tint = Color.Red,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

        )
        // Video Player or Media Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF1E1B4B)),
            contentAlignment = Alignment.Center
        ) {
            when {
                room?.videoType == "none" || room?.videoUrl.isNullOrEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(0.5f)
                        )
                        Text(
                            "No media selected", color = Color.White.copy(0.7f), fontSize = 18.sp
                        )
                        Button(
                            onClick = { showMediaSelector = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Select Media")
                        }
                    }
                }
                // YouTube video
                room?.videoType == "youtube" -> {
                    val videoId = getYouTubeVideoId(room?.videoUrl ?: "")
                    if (videoId != null) {
                        AndroidView(
                            factory = { ctx ->
                                YouTubePlayerView(ctx).apply {
                                    enableAutomaticInitialization = false

                                    initialize(object : AbstractYouTubePlayerListener() {
                                        override fun onReady(youTubePlayer: YouTubePlayer) {
                                            viewModel.youtubePlayer = youTubePlayer
                                            viewModel.isYoutubePlayerReady = true
                                            isPlayerReady = true

                                            val startTime = (room?.currentTime ?: 0) / 1000f
                                            youTubePlayer.cueVideo(videoId, startTime)

                                            if (room?.isPlaying == true) {
                                                youTubePlayer.play()
                                            }
                                        }

                                        override fun onCurrentSecond(
                                            youTubePlayer: YouTubePlayer, second: Float
                                        ) {
                                            currentYoutubeTime = second
                                            viewModel.currentYoutubeTime = second
                                        }

                                        override fun onStateChange(
                                            youTubePlayer: YouTubePlayer,
                                            state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
                                        ) {
                                            if (!isPlayerReady) return

                                            coroutineScope.launch {
                                                delay(500)

                                                when (state) {
                                                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> {
                                                        if (room?.isPlaying == false) {
                                                            viewModel.updatePlaybackState(
                                                                true,
                                                                (currentYoutubeTime * 1000).toLong()
                                                            )
                                                        }
                                                    }

                                                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> {
                                                        if (room?.isPlaying == true) {
                                                            viewModel.updatePlaybackState(
                                                                false,
                                                                (currentYoutubeTime * 1000).toLong()
                                                            )
                                                        }
                                                    }

                                                    else -> {}
                                                }
                                            }
                                        }
                                    })
                                }
                            }, modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Direct URL video
                room?.videoType == "url" -> {
                    LaunchedEffect(Unit) {
                        viewModel.exoPlayer = ExoPlayer.Builder(context).build().apply {
                            room?.videoUrl?.let {
                                setMediaItem(MediaItem.fromUri(it))
                                prepare()
                                seekTo(room?.currentTime ?: 0)
                                if (room?.isPlaying == true) play()
                            }
                        }
                    }

                    AndroidView(
                        factory = {
                            PlayerView(it).apply {
                                player = viewModel.exoPlayer
                                useController = true
                                controllerAutoShow = false
                            }
                        }, modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Controls
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        room?.roomName ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${room?.members?.size ?: 0} watching",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }


                // Change Media button
                if (room?.videoType != "none" && !room?.videoUrl.isNullOrEmpty()) {
                    IconButton(onClick = { showMediaSelector = true }) {
                        Icon(
                            Icons.Default.ChangeCircle,
                            "Change Media",
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(onClick = { showChat = !showChat }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        "Toggle Chat",
                        tint = if (showChat) Color(0xFFEC4899) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Chat Section - Takes remaining space
        if (showChat) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
            ) {
                // Messages - Scrollable
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages.reversed()) { message ->
                        val isMe = message.senderId == user?.uid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) Color(0xFFEC4899) else Color(
                                        0xFF1E293B
                                    )
                                ), shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (!isMe) {
                                        Text(
                                            message.senderName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEC4899)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    Text(
                                        message.message, color = Color.White, fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Message Input - Fixed at bottom
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    color = Color(0xFF1E293B),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendRoomMessage(messageText)
                                        messageText = ""
                                    }
                                }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFEC4899),
                                unfocusedBorderColor = Color.Gray,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3)
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendRoomMessage(messageText)
                                    messageText = ""
                                }
                            }, enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                null,
                                tint = if (messageText.isNotBlank()) Color(0xFFEC4899) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Members List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Members",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(room?.members?.values?.toList() ?: emptyList()) { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEC4899)), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                member.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                member.username, color = Color.White, fontWeight = FontWeight.Medium
                            )
                            val isOnline = System.currentTimeMillis() - member.lastSeen < 5000
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color.Green else Color.Gray)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (isOnline) "Online" else "Offline",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showMediaSelector) {
        AlertDialog(
            onDismissRequest = { showMediaSelector = false },
            title = { Text("Select Media Type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            showMediaSelector = false
                            showYouTubeSearch = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.VideoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("YouTube")
                    }

                    Button(
                        onClick = {
                            showMediaSelector = false
                            // TODO: Add file picker or URL input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Icon(Icons.Default.VideoFile, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Direct URL")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMediaSelector = false }) { Text("Cancel") }
            })
    }

    if (showYouTubeSearch) {
        var searchQuery by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showYouTubeSearch = false },
            title = { Text("Search YouTube") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Enter YouTube URL or search") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://youtube.com/watch?v=...") })

                    Text(
                        "Paste a YouTube link above", fontSize = 12.sp, color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.selectMedia(searchQuery)
                        showYouTubeSearch = false
                    }
                }) {
                    Text("Load Video")
                }
            },
            dismissButton = {
                TextButton(onClick = { showYouTubeSearch = false }) { Text("Cancel") }
            })
    }
}