package com.techaventus.abc.view

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.techaventus.abc.viewmodel.KosmiViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun RoomScreen(viewModel: KosmiViewModel) {
    val context = LocalContext.current
    val room by viewModel.currentRoom.collectAsState()
    val messages by viewModel.roomMessages.collectAsState()
    val user by viewModel.user.collectAsState()
    var isPlayerReady by remember { mutableStateOf(false) }
    var currentYoutubeTime by remember { mutableStateOf(0f) }
    var lastTimeUpdate by remember { mutableStateOf(0L) }
    var messageText by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.exoPlayer?.release()
            viewModel.exoPlayer = null
            viewModel.youtubePlayer = null
            viewModel.isYoutubePlayerReady = false
        }
    }

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

    // Use Box with fixed layout instead of Column
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Video Player - Fixed at top
            when (room?.videoType) {
                "youtube" -> {
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
                                            youTubePlayer: YouTubePlayer,
                                            second: Float
                                        ) {
                                            currentYoutubeTime = second
                                            viewModel.currentYoutubeTime = second

                                            val now = System.currentTimeMillis()
                                            if (now - lastTimeUpdate > 5000) {
                                                viewModel.updateYouTubeTime(second)
                                                lastTimeUpdate = now
                                            }
                                        }

                                        override fun onStateChange(
                                            youTubePlayer: YouTubePlayer,
                                            state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
                                        ) {
                                            if (!isPlayerReady) return

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
                                    })
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Invalid YouTube URL", color = Color.White)
                        }
                    }
                }

                else -> {
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(5000)
                            viewModel.exoPlayer?.let { player ->
                                if (player.isPlaying) {
                                    viewModel.updatePlaybackState(true, player.currentPosition)
                                }
                            }
                        }
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
                    IconButton(onClick = {
                        when (room?.videoType) {
                            "youtube" -> {
                                viewModel.youtubePlayer?.let { player ->
                                    val newIsPlaying = !(room?.isPlaying ?: false)
                                    viewModel.updatePlaybackState(
                                        newIsPlaying,
                                        (currentYoutubeTime * 1000).toLong()
                                    )

                                    if (newIsPlaying) {
                                        player.play()
                                    } else {
                                        player.pause()
                                    }
                                }
                            }

                            else -> {
                                viewModel.exoPlayer?.let { player ->
                                    val newIsPlaying = !player.isPlaying
                                    if (newIsPlaying) {
                                        player.play()
                                    } else {
                                        player.pause()
                                    }
                                    viewModel.updatePlaybackState(
                                        newIsPlaying,
                                        player.currentPosition
                                    )
                                }
                            }
                        }
                    }) {
                        Icon(
                            if (room?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

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

                    IconButton(onClick = { showChat = !showChat }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            "Toggle Chat",
                            tint = if (showChat) Color(0xFFEC4899) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.leaveRoom() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(28.dp)
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
                                    ),
                                    shape = RoundedCornerShape(12.dp)
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
                                            message.message,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Message Input - Fixed at bottom
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFEC4899),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendRoomMessage(messageText)
                                        messageText = ""
                                    }
                                },
                                enabled = messageText.isNotBlank()
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
                                    .background(Color(0xFFEC4899)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    member.username.firstOrNull()?.uppercaseChar()?.toString()
                                        ?: "?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    member.username,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
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
    }
}