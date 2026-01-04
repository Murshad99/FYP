package com.techaventus.fyp.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.techaventus.fyp.model.AppNotification
import com.techaventus.fyp.model.ChatMessage
import com.techaventus.fyp.model.Friend
import com.techaventus.fyp.model.FriendRequest
import com.techaventus.fyp.model.Member
import com.techaventus.fyp.model.RoomData
import com.techaventus.fyp.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VM : ViewModel() {

    private val _currentFriendChat = MutableStateFlow<Friend?>(null)
    val currentFriendChat: StateFlow<Friend?> = _currentFriendChat
    private val _friendChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val friendChatMessages: StateFlow<List<ChatMessage>> = _friendChatMessages
    private var friendChatListener: ValueEventListener? = null
    private val auth: FirebaseAuth = Firebase.auth
    private val database: DatabaseReference = Firebase.database.reference
    private val _screen = MutableStateFlow("auth")
    val screen: StateFlow<String> = _screen
    private val _bottomTab = MutableStateFlow("rooms")
    val bottomTab: StateFlow<String> = _bottomTab
    private val _user = MutableStateFlow(auth.currentUser)
    val user = _user
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile
    private val _rooms = MutableStateFlow<List<RoomData>>(emptyList())
    val rooms: StateFlow<List<RoomData>> = _rooms
    private val _myRooms = MutableStateFlow<List<RoomData>>(emptyList())
    val myRooms: StateFlow<List<RoomData>> = _myRooms
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests

    private val _currentRoom = MutableStateFlow<RoomData?>(null)
    val currentRoom: StateFlow<RoomData?> = _currentRoom

    private val _roomMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val roomMessages: StateFlow<List<ChatMessage>> = _roomMessages

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications = _hasUnreadNotifications.asStateFlow()

    var exoPlayer: ExoPlayer? = null
    var youtubePlayer: com.pierfrancescosoffritti.
    androidyoutubeplayer.core.player.
    YouTubePlayer? = null
    private var roomListener: ValueEventListener? = null
    private var chatListener: ValueEventListener? = null
    private var isSyncing = false
    var currentYoutubeTime = 0f
    var isYoutubePlayerReady = false

    private var lastUpdateTime = 0L
    private var lastUpdateState: Pair<Boolean, Long>? = null // Track last update

    init {
        if (auth.currentUser != null) {
            _screen.value = "main"
            loadUserProfile()
            fetchPublicRooms()
            fetchMyRooms()
            fetchFriends()
            fetchFriendRequests()
            initFriendsListener()
            listenNotifications()
        }
    }


    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    val profile = UserProfile(
                        userId = user.uid,
                        username = username,
                        email = email
                    )
                    database.child("users").child(user.uid).setValue(profile).await()
                }
                _user.value = auth.currentUser
                loadUserProfile()
                _screen.value = "main"
                fetchPublicRooms()
                fetchMyRooms()
                fetchFriends()
                fetchFriendRequests()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                auth.signInWithEmailAndPassword(email, password).await()
                _user.value = auth.currentUser
                loadUserProfile()
                _screen.value = "main"
                fetchPublicRooms()
                fetchMyRooms()
                fetchFriends()
                fetchFriendRequests()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { user ->
                    val snapshot = database.child("users").child(user.uid).get().await()
                    if (!snapshot.exists()) {
                        val profile = UserProfile(
                            userId = user.uid,
                            username = user.displayName ?: "User",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString() ?: ""
                        )
                        database.child("users").child(user.uid).setValue(profile).await()
                    }
                }
                _user.value = auth.currentUser
                loadUserProfile()
                _screen.value = "main"
                fetchPublicRooms()
                fetchFriends()
                fetchFriendRequests()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
        _userProfile.value = null
        _screen.value = "auth"
        _bottomTab.value = "rooms"
    }

    private fun loadUserProfile() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val snapshot = database.child("users").child(user.uid).get().await()
                _userProfile.value = snapshot.getValue(UserProfile::class.java)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateProfile(username: String, bio: String, password: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                _loading.value = true
                val updates = mapOf("username" to username, "bio" to bio, "password" to password)
                database.child("users").child(user.uid).updateChildren(updates).await()
                loadUserProfile()
                _screen.value = "main"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createRoom(roomName: String, isPublic: Boolean) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val user = auth.currentUser ?: return@launch
                val roomId = database.child("rooms").push().key ?: return@launch
                val profile = _userProfile.value

                val room = RoomData(
                    roomId = roomId,
                    roomName = roomName,
                    videoUrl = "", // Empty initially
                    videoType = "none", // No video yet
                    isPublic = isPublic,
                    creator = profile?.username ?: "User",
                    creatorId = user.uid,
                    members = mapOf(
                        user.uid to Member(
                            userId = user.uid,
                            username = profile?.username ?: "User"
                        )
                    )
                )

                database.child("rooms").child(roomId).setValue(room).await()
                fetchPublicRooms()
                fetchMyRooms()
                joinRoom(roomId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun joinRoom(roomId: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                val profile = _userProfile.value

                // Reset state
                isSyncing = false
                lastUpdateState = null

                // First get room data
                val snapshot = database.child("rooms").child(roomId).get().await()
                val room = snapshot.getValue(RoomData::class.java)

                if (room == null) {
                    _error.value = "Room not found"
                    return@launch
                }

                // Set current room BEFORE adding listener
                _currentRoom.value = room

                // Add member to room
                val member = Member(
                    userId = user.uid,
                    username = profile?.username ?: "User",
                    lastSeen = System.currentTimeMillis()
                )

                database.child("rooms").child(roomId)
                    .child("members").child(user.uid)
                    .setValue(member).await()

                // Listen to room state changes for real-time sync
                roomListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val updatedRoom = snapshot.getValue(RoomData::class.java)
                        val oldRoom = _currentRoom.value

                        updatedRoom?.let { newRoom ->
                            if (oldRoom != null) {
                                val playStateChanged = oldRoom.isPlaying != newRoom.isPlaying
                                val timeDiff =
                                    kotlin.math.abs(oldRoom.currentTime - newRoom.currentTime)
                                val timeChanged = timeDiff > 3000 // 3 seconds threshold

                                // Update local state first
                                _currentRoom.value = newRoom

                                // Only sync if there's a significant change
                                if (playStateChanged || timeChanged) {
                                    syncVideoToState(newRoom)
                                }
                            } else {
                                _currentRoom.value = newRoom
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _error.value = error.message
                    }
                }

                database.child("rooms").child(roomId).addValueEventListener(roomListener!!)

                // Setup chat listener
                chatListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages = mutableListOf<ChatMessage>()
                        snapshot.children.forEach { child ->
                            child.getValue(ChatMessage::class.java)?.let { messages.add(it) }
                        }
                        _roomMessages.value = messages.sortedBy { it.timestamp }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
                database.child("messages").child(roomId).addValueEventListener(chatListener!!)

                _screen.value = "room"

                // Start presence update
                startPresenceUpdate(roomId)

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun startPresenceUpdate(roomId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            while (_currentRoom.value != null) {
                try {
                    val updates = mapOf(
                        "lastSeen" to System.currentTimeMillis()
                    )
                    database.child("rooms").child(roomId)
                        .child("members").child(user.uid)
                        .updateChildren(updates).await()

                    println("DEBUG: Presence updated for user: ${user.uid}")
                    delay(3000)
                } catch (e: Exception) {
                    println("DEBUG: Presence update failed: ${e.message}")
                }
            }
        }
    }

    fun sendRoomMessage(message: String) {
        val room = _currentRoom.value ?: return
        val user = auth.currentUser ?: return
        val profile = _userProfile.value

        viewModelScope.launch {
            try {
                val messageId =
                    database.child("messages").child(room.roomId).push().key ?: return@launch
                val chatMessage = ChatMessage(
                    messageId = messageId,
                    senderId = user.uid,
                    senderName = profile?.username ?: "User",
                    message = message
                )
                database.child("messages").child(room.roomId).child(messageId).setValue(chatMessage)
                    .await()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun syncVideoToState(room: RoomData) {
        if (isSyncing) return

        viewModelScope.launch {
            isSyncing = true

            when (room.videoType) {
                "youtube" -> {
                    youtubePlayer?.let { player ->
                        if (isYoutubePlayerReady) {
                            val targetTime = room.currentTime / 1000f
                            val timeDiff = kotlin.math.abs(currentYoutubeTime - targetTime)

                            if (timeDiff > 5) {
                                player.seekTo(targetTime)
                                println("DEBUG: Synced time to $targetTime")
                            }
                        }
                    }
                }

                else -> {
                    exoPlayer?.let { player ->
                        val timeDiff = kotlin.math.abs(player.currentPosition - room.currentTime)
                        if (timeDiff > 5000) {
                            player.seekTo(room.currentTime)
                        }
                    }
                }
            }

            delay(500)
            isSyncing = false
        }
    }

    fun updatePlaybackState(isPlaying: Boolean, currentTime: Long) {
        val room = _currentRoom.value ?: return

        // Check if state actually changed
        val lastState = lastUpdateState
        if (lastState != null && lastState.first == isPlaying && kotlin.math.abs(lastState.second - currentTime) < 1000) {
            println("DEBUG: State unchanged, skipping update")
            return
        }

        // Debouncing: Don't update too frequently
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < 1000) { // Increased to 1 second
            println("DEBUG: Skipping update (too frequent)")
            return
        }
        lastUpdateTime = now
        lastUpdateState = Pair(isPlaying, currentTime)

        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "isPlaying" to isPlaying,
                    "currentTime" to currentTime
                )
                database.child("rooms").child(room.roomId).updateChildren(updates).await()
                println("DEBUG: Updated Firebase - Playing: $isPlaying, Time: $currentTime")
            } catch (e: Exception) {
                _error.value = e.message
                println("DEBUG: Firebase update failed: ${e.message}")
            }
        }
    }

    fun leaveRoom() {
        val room = _currentRoom.value
        if (room != null) {
            roomListener?.let {
                database.child("rooms").child(room.roomId).removeEventListener(it)
            }
            chatListener?.let {
                database.child("messages").child(room.roomId).removeEventListener(it)
            }
        }

        // Reset all state variables
        _currentRoom.value = null
        _roomMessages.value = emptyList()
        exoPlayer?.release()
        exoPlayer = null
        youtubePlayer = null
        isYoutubePlayerReady = false
        isSyncing = false
        lastUpdateState = null
        lastUpdateTime = 0L
        roomListener = null
        chatListener = null
        _screen.value = "main"

        println("DEBUG: Left room, all state reset")
    }

    fun fetchPublicRooms() {
        database.child("rooms")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomsList = mutableListOf<RoomData>()
                    snapshot.children.forEach { child ->
                        val room = child.getValue(RoomData::class.java)
                        if (room != null && room.isPublic) {
                            roomsList.add(room)
                        }
                    }
                    _rooms.value = roomsList.sortedByDescending { it.createdAt }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Failed to load rooms: ${error.message}"
                }
            }
            )
    }

    fun fetchMyRooms() {
        val user = auth.currentUser ?: return
        database.child("rooms").orderByChild("creatorId").equalTo(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomsList = mutableListOf<RoomData>()
                    snapshot.children.forEach { child ->
                        val room = child.getValue(RoomData::class.java)
                        room?.let { roomsList.add(it) }
                    }
                    _myRooms.value = roomsList.sortedByDescending { it.createdAt }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.value = "My rooms load error: ${error.message}"
                }
            })
    }
    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun deleteRoom(roomId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _loading.value = true

                val snapshot = database.child("rooms").child(roomId).get().await()
                val room = snapshot.getValue(RoomData::class.java)

                if (room == null) {
                    _error.value = "Room already deleted"
                    return@launch
                }

                if (room.creatorId != userId) {
                    _error.value = "You are not allowed to delete this room"
                    return@launch
                }

                database.child("rooms").child(roomId).removeValue().await()

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchUserByUserName(
        username: String,
        callback: (List<UserProfile>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val snapshot = database.child("users")
                    .orderByChild("username")
                    .startAt(username)
                    .endAt(username + "\uf8ff")
                    .get()
                    .await()

                val users = snapshot.children.mapNotNull {
                    it.getValue(UserProfile::class.java)
                }

                callback(users)
            } catch (e: Exception) {
                callback(emptyList())
            }
        }
    }

    fun sendFriendRequest(toUserId: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                val profile = _userProfile.value ?: return@launch
                val requestId = database.child("friend_requests").push().key ?: return@launch

                val request = FriendRequest(
                    requestId = requestId,
                    fromUserId = user.uid,
                    fromUsername = profile.username,
                    toUserId = toUserId
                )

                database.child("friend_requests").child(requestId).setValue(request).await()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            try {
                // Update friend request status
                database.child("friend_requests").child(request.requestId)
                    .child("status").setValue("accepted").await()

                // Add each other as friends
                database.child("friends").child(request.toUserId).child(request.fromUserId)
                    .setValue(true).await()
                database.child("friends").child(request.fromUserId).child(request.toUserId)
                    .setValue(true).await()

                pushNotification(
                    toUserId = request.fromUserId,
                    type = "friend_accept",
                    title = "Friend request accepted",
                    message = "${_userProfile.value?.username} accepted your request",
                    relatedId = request.toUserId
                )

                // Fetch updated friends lists
                fetchFriends()         // Current user
                if (request.toUserId == auth.currentUser?.uid) {
                    fetchFriends()     // Optional: double call ensures recomposition
                }

                fetchFriendRequests()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun rejectFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            try {
                database.child("friend_requests").child(request.requestId).child("status")
                    .setValue("rejected").await()
                pushNotification(
                    toUserId = request.fromUserId,
                    type = "friend_reject",
                    title = "Friend request rejected",
                    message = "${_userProfile.value?.username} rejected your request",
                    relatedId = request.toUserId
                )
                fetchFriendRequests()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun fetchFriends() {
        val user = auth.currentUser ?: return
        database.child("friends").child(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModelScope.launch {
                        val friendsList = mutableListOf<Friend>()
                        snapshot.children.forEach { child ->
                            val friendId = child.key ?: return@forEach
                            val userSnapshot = database.child("users").child(friendId).get().await()
                            userSnapshot.getValue(UserProfile::class.java)?.let { profile ->
                                friendsList.add(
                                    Friend(
                                        userId = profile.userId,
                                        username = profile.username,
                                        photoUrl = profile.photoUrl
                                    )
                                )
                            }
                        }
                        _friends.value = friendsList
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun fetchFriendRequests() {
        val user = auth.currentUser ?: return
        database.child("friend_requests").orderByChild("toUserId").equalTo(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requests = mutableListOf<FriendRequest>()
                    snapshot.children.forEach { child ->
                        child.getValue(FriendRequest::class.java)?.let {
                            if (it.status == "pending") requests.add(it)
                        }
                    }
                    _friendRequests.value = requests
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun initFriendsListener() {
        val user = auth.currentUser ?: return
        database.child("friends").child(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModelScope.launch {
                        val friendsList = mutableListOf<Friend>()
                        snapshot.children.forEach { child ->
                            val friendId = child.key ?: return@forEach
                            val userSnapshot = database.child("users").child(friendId).get().await()
                            userSnapshot.getValue(UserProfile::class.java)?.let { profile ->
                                friendsList.add(
                                    Friend(
                                        userId = profile.userId,
                                        username = profile.username,
                                        photoUrl = profile.photoUrl
                                    )
                                )
                            }
                        }
                        _friends.value = friendsList
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun setScreen(screen: String) {
        _screen.value = screen
    }

    fun setBottomTab(tab: String) {
        _bottomTab.value = tab
    }

    fun getJoinRoomLink(roomId: String): String {
        return "https://watchtogether.com/join?roomId=$roomId"
    }

    fun extractRoomIdFromUrl(url: String): String? {
        return try {
            val uri = url.toUri()
            uri.getQueryParameter("roomId")
        } catch (e: Exception) {
            null
        }
    }

    fun getFriendRoomId(friend: Friend): String {
        val currentUser = currentUserId ?: return ""
        return listOf(currentUser, friend.userId).sorted().joinToString("_")
    }

    fun joinFriendChat(friend: Friend) {
        val roomId = getFriendRoomId(friend)

        viewModelScope.launch {
            try {
                // Set current friend chat
                _currentFriendChat.value = friend
                friendChatListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages = mutableListOf<ChatMessage>()
                        snapshot.children.forEach { child ->
                            child.getValue(ChatMessage::class.java)?.let { messages.add(it) }
                        }
                        _friendChatMessages.value = messages.sortedBy { it.timestamp }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _error.value = error.message
                    }
                }
                database.child("friend_chats").child(roomId)
                    .addValueEventListener(friendChatListener!!)

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun sendFriendChatMessage(message: String) {
        val friend = _currentFriendChat.value ?: return
        val user = auth.currentUser ?: return
        val profile = _userProfile.value ?: return
        val roomId = getFriendRoomId(friend)

        viewModelScope.launch {
            try {
                val messageId =
                    database.child("friend_chats").child(roomId).push().key ?: return@launch
                val chatMessage = ChatMessage(
                    messageId = messageId,
                    senderId = user.uid,
                    senderName = profile.username,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
                database.child("friend_chats").child(roomId).child(messageId).setValue(chatMessage)
                    .await()

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun leaveFriendChat() {
        val friend = _currentFriendChat.value ?: return
        val roomId = getFriendRoomId(friend)

        friendChatListener?.let {
            database.child("friend_chats").child(roomId).removeEventListener(it)
        }
        _currentFriendChat.value = null
        _friendChatMessages.value = emptyList()
        friendChatListener = null
    }

    fun selectMedia(videoUrl: String) {
        val room = _currentRoom.value ?: return

        viewModelScope.launch {
            try {
                // Detect video type
                val videoType = when {
                    videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be") -> "youtube"
                    videoUrl.isNotEmpty() -> "url"
                    else -> "none"
                }

                val updates = mapOf(
                    "videoUrl" to videoUrl,
                    "videoType" to videoType,
                    "currentTime" to 0L,
                    "isPlaying" to false
                )

                database.child("rooms").child(room.roomId).updateChildren(updates).await()

                // Update local state
                _currentRoom.value = room.copy(
                    videoUrl = videoUrl,
                    videoType = videoType,
                    currentTime = 0,
                    isPlaying = false
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun completeSplash() {
        _showSplash.value = false
    }

    fun listenNotifications() {
        val user = auth.currentUser ?: return

        database.child("notifications").child(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<AppNotification>()

                    snapshot.children.forEach { child ->
                        child.getValue(AppNotification::class.java)?.let {
                            list.add(it.copy(id = child.key ?: ""))
                        }
                    }

                    _notifications.value = list.sortedByDescending { it.timestamp }
                    _hasUnreadNotifications.value = list.any { !it.read }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun markNotificationRead(notificationId: String) {
        val user = auth.currentUser ?: return
        database.child("notifications")
            .child(user.uid)
            .child(notificationId)
            .child("read")
            .setValue(true)
    }

    private fun pushNotification(
        toUserId: String,
        type: String,
        title: String,
        message: String,
        relatedId: String
    ) {
        val id = database.child("notifications").child(toUserId).push().key ?: return

        val notification = AppNotification(
            id = id,
            type = type,
            title = title,
            message = message,
            relatedId = relatedId,
            read = false,
            timestamp = System.currentTimeMillis()
        )

        database.child("notifications")
            .child(toUserId)
            .child(id)
            .setValue(notification)
    }
}