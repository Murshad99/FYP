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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@OptIn(UnstableApi::class)
@Composable
fun RoomScreen(viewModel: KosmiViewModel) {
    val context = LocalContext.current
    val room by viewModel.currentRoom.collectAsState()
    var isPlayerReady by remember { mutableStateOf(false) }
    var currentYoutubeTime by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.exoPlayer?.release()
            viewModel.exoPlayer = null
            viewModel.youtubePlayer = null
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

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        // Video Player
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
                                        isPlayerReady = true

                                        // Load video
                                        youTubePlayer.cueVideo(videoId, (room?.currentTime ?: 0) / 1000f)

                                        // Auto play if room state says playing
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

                    // Sync YouTube player state
                    LaunchedEffect(room?.isPlaying, room?.currentTime) {
                        if (!isPlayerReady) return@LaunchedEffect

                        viewModel.youtubePlayer?.let { player ->
                            room?.let { roomData ->
                                val currentTimeMs = (currentYoutubeTime * 1000).toLong()
                                val timeDiff = kotlin.math.abs(currentTimeMs - roomData.currentTime)

                                // Seek if time difference is more than 3 seconds
                                if (timeDiff > 3000) {
                                    player.seekTo(roomData.currentTime / 1000f)
                                }
                            }
                        }
                    }
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
                // Regular video player
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

                // Sync ExoPlayer state
                LaunchedEffect(room?.isPlaying, room?.currentTime) {
                    viewModel.exoPlayer?.let { player ->
                        room?.let { roomData ->
                            val timeDiff =
                                kotlin.math.abs(player.currentPosition - roomData.currentTime)
                            if (timeDiff > 2000) {
                                player.seekTo(roomData.currentTime)
                            }
                            if (roomData.isPlaying && !player.isPlaying) {
                                player.play()
                            } else if (!roomData.isPlaying && player.isPlaying) {
                                player.pause()
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

                // Play/Pause Button
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
                                viewModel.updatePlaybackState(newIsPlaying, player.currentPosition)
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

                Column {
                    Text(
                        room?.roomName ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "${room?.members?.size ?: 0} watching",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = { viewModel.leaveRoom() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        null,
                        tint = Color.Red,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

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
                    "Members", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White,
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
                            member.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(member.username, color = Color.White, fontWeight = FontWeight.Medium)
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
