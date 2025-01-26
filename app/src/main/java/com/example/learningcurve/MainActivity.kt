package com.example.learningcurve

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.example.learningcurve.auth.AuthViewModel
import com.example.learningcurve.chatbot.ChatViewModel
import com.example.learningcurve.ui.theme.LearningCurveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setContent {
            LearningCurveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RequestMicrophonePermission {  }
                    MyApplication(modifier = Modifier.padding(innerPadding), authViewModel= AuthViewModel(), chatViewModel = ChatViewModel())
                }
            }
        }
    }
}
@Composable
fun RequestMicrophonePermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.RECORD_AUDIO)
    }
}

