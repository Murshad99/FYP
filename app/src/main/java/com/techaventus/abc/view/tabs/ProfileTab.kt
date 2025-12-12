package com.techaventus.abc.view.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techaventus.abc.viewmodel.KosmiViewModel

@Composable
fun ProfileTab(viewModel: KosmiViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0F172A))) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
            Text(
                "Profile",
                modifier = Modifier.padding(16.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEC4899)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile?.username?.first()?.toString() ?: "U",
                    fontSize = 48.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                profile?.username ?: "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(profile?.email ?: "", color = Color.Gray)
            if (!profile?.bio.isNullOrEmpty()) Text(
                profile?.bio ?: "",
                color = Color.White.copy(0.8f)
            )

            Button(
                onClick = { viewModel.setScreen("edit_profile") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
            ) {
                Text("Edit Profile")
            }
            Button(
                onClick = { viewModel.signOut() }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Logout")
            }
        }
    }
}
