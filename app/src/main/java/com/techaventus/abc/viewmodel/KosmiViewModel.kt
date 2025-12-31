package com.techaventus.abc.viewmodel

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
import com.techaventus.abc.model.ChatMessage
import com.techaventus.abc.model.Friend
import com.techaventus.abc.model.FriendRequest
import com.techaventus.abc.model.Member
import com.techaventus.abc.model.RoomData
import com.techaventus.abc.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ViewModel
class KosmiViewModel : ViewModel() {
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

    var exoPlayer: ExoPlayer? = null
    var youtubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer? =
        null
    private var roomListener: ValueEventListener? = null
    private var chatListener: ValueEventListener? = null
    private var lastSyncTime = 0L
    var currentYoutubeTime = 0f
    var isYoutubePlayerReady = false
    private var shouldUpdateFirebase = true // Control when to update Firebase

    init {
        if (auth.currentUser != null) {
            _screen.value = "main"
            loadUserProfile()
            fetchPublicRooms()
            fetchMyRooms()
            fetchFriends()
            fetchFriendRequests()
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

    fun updateProfile(username: String, bio: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                _loading.value = true
                val updates = mapOf("username" to username, "bio" to bio)
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

    fun createRoom(roomName: String, videoUrl: String, isPublic: Boolean) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val user = auth.currentUser ?: return@launch
                val roomId = database.child("rooms").push().key ?: return@launch
                val profile = _userProfile.value

                // Detect video type
                val videoType =
                    if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
                        "youtube"
                    } else {
                        "url"
                    }

                val room = RoomData(
                    roomId = roomId,
                    roomName = roomName,
                    videoUrl = videoUrl,
                    videoType = videoType,
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
                fetchMyRooms() // Refresh my rooms list
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
                database.child("rooms").child(roomId).child("members").child(user.uid)
                    .setValue(Member(userId = user.uid, username = profile?.username ?: "User"))
                    .await()

                // Listen to room state changes for real-time sync
                roomListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val room = snapshot.getValue(RoomData::class.java)
                        val oldRoom = _currentRoom.value
                        _currentRoom.value = room

                        // Only sync if state changed from another user
                        room?.let { newRoom ->
                            if (oldRoom != null) {
                                // Check if update came from another user
                                val playStateChanged = oldRoom.isPlaying != newRoom.isPlaying
                                val timeChanged =
                                    kotlin.math.abs(oldRoom.currentTime - newRoom.currentTime) > 2000

                                if (playStateChanged || timeChanged) {
                                    syncVideoToState(newRoom)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _error.value = error.message
                    }
                }

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

                database.child("rooms").child(roomId).addValueEventListener(roomListener!!)

                val snapshot = database.child("rooms").child(roomId).get().await()
                _currentRoom.value = snapshot.getValue(RoomData::class.java)
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
                    database.child("rooms").child(roomId)
                        .child("members").child(user.uid)
                        .child("lastSeen").setValue(System.currentTimeMillis())
                    delay(3000) // Update every 3 seconds
                } catch (e: Exception) {
                    // Silently fail
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
                val messageId = database.child("messages").child(room.roomId).push().key ?: return@launch
                val chatMessage = ChatMessage(
                    messageId = messageId,
                    senderId = user.uid,
                    senderName = profile?.username ?: "User",
                    message = message
                )
                database.child("messages").child(room.roomId).child(messageId).setValue(chatMessage).await()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun syncVideoToState(room: RoomData) {
        when (room.videoType) {
            "youtube" -> {
                youtubePlayer?.let { player ->
                    if (!isYoutubePlayerReady) return

                    shouldUpdateFirebase = false // Don't update Firebase during sync

                    // Sync time
                    val targetTime = room.currentTime / 1000f
                    val timeDiff = kotlin.math.abs(currentYoutubeTime - targetTime)
                    if (timeDiff > 3) {
                        player.seekTo(targetTime)
                    }

                    // Sync play/pause state
                    if (room.isPlaying) {
                        player.play()
                    } else {
                        player.pause()
                    }

                    viewModelScope.launch {
                        delay(1000)
                        shouldUpdateFirebase = true // Re-enable Firebase updates
                    }
                }
            }

            else -> {
                exoPlayer?.let { player ->
                    val timeDiff = kotlin.math.abs(player.currentPosition - room.currentTime)
                    if (timeDiff > 2000) {
                        player.seekTo(room.currentTime)
                    }
                    if (room.isPlaying && !player.isPlaying) {
                        player.play()
                    } else if (!room.isPlaying && player.isPlaying) {
                        player.pause()
                    }
                }
            }
        }
    }

    fun updatePlaybackState(isPlaying: Boolean, currentTime: Long) {
        val room = _currentRoom.value ?: return
        if (!shouldUpdateFirebase) return // Don't update during sync

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
            }
        }
    }

    // Periodic time update for YouTube (called every 5 seconds)
    fun updateYouTubeTime(currentTime: Float) {
        val room = _currentRoom.value ?: return
        if (room.videoType != "youtube" || !shouldUpdateFirebase) return

        viewModelScope.launch {
            try {
                database.child("rooms").child(room.roomId)
                    .child("currentTime").setValue((currentTime * 1000).toLong())
            } catch (e: Exception) {
                // Silently fail for periodic updates
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

        _currentRoom.value = null
        _roomMessages.value = emptyList() // Clear messages
        exoPlayer?.release()
        exoPlayer = null
        youtubePlayer = null
        isYoutubePlayerReady = false
        shouldUpdateFirebase = true
        roomListener = null
        chatListener = null
        _screen.value = "main"
    }
    fun fetchPublicRooms() {
        database.child("rooms").orderByChild("isPublic").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomsList = mutableListOf<RoomData>()
                    snapshot.children.forEach { child ->
                        child.getValue(RoomData::class.java)?.let {
                            roomsList.add(it)
                            println("DEBUG: Public Room loaded: ${it.roomName}")
                        }
                    }
                    _rooms.value = roomsList.sortedByDescending { it.createdAt }
                    println("DEBUG: Total public rooms: ${roomsList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    println("DEBUG: Error loading public rooms: ${error.message}")
                    _error.value = "Public rooms load error: ${error.message}"
                }
            })
    }

    fun fetchMyRooms() {
        val user = auth.currentUser ?: return
        println("DEBUG: Fetching rooms for user: ${user.uid}")

        database.child("rooms").orderByChild("creatorId").equalTo(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomsList = mutableListOf<RoomData>()
                    println("DEBUG: Snapshot exists: ${snapshot.exists()}")
                    println("DEBUG: Snapshot children count: ${snapshot.childrenCount}")

                    snapshot.children.forEach { child ->
                        val room = child.getValue(RoomData::class.java)
                        println("DEBUG: Room data: $room")
                        room?.let {
                            roomsList.add(it)
                            println("DEBUG: My Room loaded: ${it.roomName} by ${it.creator}")
                        }
                    }
                    _myRooms.value = roomsList.sortedByDescending { it.createdAt }
                    println("DEBUG: Total my rooms: ${roomsList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    println("DEBUG: Error loading my rooms: ${error.message}")
                    _error.value = "My rooms load error: ${error.message}"
                }
            })
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                database.child("rooms").child(roomId).removeValue().await()
                fetchMyRooms()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchUserByEmail(email: String, callback: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot =
                    database.child("users").orderByChild("email").equalTo(email).get().await()
                val profile = snapshot.children.firstOrNull()?.getValue(UserProfile::class.java)
                callback(profile)
            } catch (e: Exception) {
                callback(null)
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
                database.child("friend_requests").child(request.requestId).child("status")
                    .setValue("accepted").await()
                database.child("friends").child(request.toUserId).child(request.fromUserId)
                    .setValue(true).await()
                database.child("friends").child(request.fromUserId).child(request.toUserId)
                    .setValue(true).await()
                fetchFriendRequests()
                fetchFriends()
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
                fetchFriendRequests()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun fetchFriends() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val snapshot = database.child("friends").child(user.uid).get().await()
                val friendsList = mutableListOf<Friend>()
                snapshot.children.forEach { child ->
                    val friendId = child.key ?: return@forEach
                    val friendProfile = database.child("users").child(friendId).get().await()
                    friendProfile.getValue(UserProfile::class.java)?.let { profile ->
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
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
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

    fun setScreen(screen: String) {
        _screen.value = screen
    }

    fun setBottomTab(tab: String) {
        _bottomTab.value = tab
    }

    fun clearError() {
        _error.value = null
    }
}