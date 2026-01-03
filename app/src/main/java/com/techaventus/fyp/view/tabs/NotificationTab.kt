package com.techaventus.fyp.view.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techaventus.fyp.model.AppNotification
import com.techaventus.fyp.viewmodel.VM


@Composable
fun NotificationsTab(viewModel: VM) {
    val notifications by viewModel.notifications.collectAsState()
    // notifications
//    val notifications = listOf(
//        NotificationItem(
//            title = "Friend Request Accepted",
//            message = "Ali accepted your friend request",
//            time = "2 min ago"
//        ),
//        NotificationItem(
//            title = "New Message",
//            message = "Ahmed sent you a new message",
//            time = "10 min ago"
//        )
//    )

    var selectedNotification by remember { mutableStateOf<AppNotification?>(null) }

    Column {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Text(
                "Notifications",
                modifier = Modifier.padding(16.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notifications) { notif ->
                NotificationCard(notification = notif) {
                    viewModel.markNotificationRead(notif.id)

                    when (notif.type) {
                        "chat" -> {
                            viewModel.setBottomTab("chats")
                        }
                        "room_invite" -> {
                            viewModel.joinRoom(notif.relatedId)
                        }
                        "friend_accept" -> {
                            viewModel.setBottomTab("chats")
                        }
                    }
                }
            }
        }
    }

    // Notification Detail Dialog
    selectedNotification?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = { Text(notif.title) },
            text = { Text(notif.message) },
            confirmButton = {
                TextButton(onClick = { selectedNotification = null }) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                Color(0xFF1E293B)
            else
                Color(0xFF334155)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                notification.title,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                notification.message,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}