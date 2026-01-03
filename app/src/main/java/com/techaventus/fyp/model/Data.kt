package com.techaventus.fyp.model

// Data Models
data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class RoomData(
    val roomId: String = "",
    val roomName: String = "",
    val videoUrl: String = "",
    val videoType: String = "none", // "url" or "youtube"
    val currentTime: Long = 0,
    val isPlaying: Boolean = false,
    val isPublic: Boolean = false,
    val creator: String = "",
    val creatorId: String = "",
    val members: Map<String, Member> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Member(
    val userId: String = "",
    val username: String = "",
    val lastSeen: Long = System.currentTimeMillis()
)

data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val toUserId: String = "",
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)

data class Friend(
    val userId: String = "",
    val username: String = "",
    val photoUrl: String = ""
)

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
data class AppNotification(
    val id: String = "",
    val type: String = "",        // chat | friend_accept | room_invite
    val title: String = "",
    val message: String = "",
    val relatedId: String = "",
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
