package com.techaventus.abc.view.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techaventus.abc.viewmodel.KosmiViewModel

@Composable
fun NotificationsTab(viewModel: KosmiViewModel) {
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Koi notification nahi", color = Color.Gray)
        }
    }
}