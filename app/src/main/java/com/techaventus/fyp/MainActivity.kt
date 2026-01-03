package com.techaventus.fyp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.techaventus.fyp.ui.theme.AbcTheme
import com.techaventus.fyp.view.App
import com.techaventus.fyp.viewmodel.VM


val viewModel: VM = VM()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)
        setContent {
            AbcTheme {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return

        if (data.host == "watchtogether.com" && data.path == "/join") {
            val roomId = data.getQueryParameter("roomId")
            if (roomId != null) {
                viewModel.joinRoom(roomId)
            }
        }
    }

}