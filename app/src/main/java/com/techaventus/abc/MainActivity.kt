package com.techaventus.abc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.techaventus.abc.ui.theme.AbcTheme
import com.techaventus.abc.view.App
import com.techaventus.abc.viewmodel.VM


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

        if (data.host == "abcapp.com" && data.path == "/join") {
            val roomId = data.getQueryParameter("roomId")
            if (roomId != null) {
                viewModel.joinRoom(roomId)
            }
        }
    }

}