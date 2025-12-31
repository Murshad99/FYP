package com.techaventus.abc.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.techaventus.abc.view.tabs.ChatsTab
import com.techaventus.abc.view.tabs.NotificationsTab
import com.techaventus.abc.view.tabs.ProfileTab
import com.techaventus.abc.view.tabs.RoomsTab
import com.techaventus.abc.viewmodel.KosmiViewModel

// Main Screen
@Composable
fun MainScreen(viewModel: KosmiViewModel) {
    val bottomTab by viewModel.bottomTab.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F172A),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E293B)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Rooms") },
                    selected = bottomTab == "rooms",
                    onClick = { viewModel.setBottomTab("rooms") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEC4899),
                        selectedTextColor = Color(0xFFEC4899),
                        indicatorColor = Color(0xFFEC4899).copy(0.2f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) },
                    label = { Text("Chats") },
                    selected = bottomTab == "chats",
                    onClick = { viewModel.setBottomTab("chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEC4899),
                        selectedTextColor = Color(0xFFEC4899),
                        indicatorColor = Color(0xFFEC4899).copy(0.2f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, null) },
                    label = { Text("Notifs") },
                    selected = bottomTab == "notifications",
                    onClick = { viewModel.setBottomTab("notifications") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEC4899),
                        selectedTextColor = Color(0xFFEC4899),
                        indicatorColor = Color(0xFFEC4899).copy(0.2f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") },
                    selected = bottomTab == "profile",
                    onClick = { viewModel.setBottomTab("profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEC4899),
                        selectedTextColor = Color(0xFFEC4899),
                        indicatorColor = Color(0xFFEC4899).copy(0.2f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (bottomTab) {
                "rooms" -> RoomsTab(viewModel)
                "chats" -> ChatsTab(viewModel)
                "notifications" -> NotificationsTab(viewModel)
                "profile" -> ProfileTab(viewModel)
            }
        }
    }
}