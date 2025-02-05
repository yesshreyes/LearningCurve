package com.example.learningcurve

import android.os.Bundle
import android.widget.Toast
import android.util.Log
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.learningcurve.auth.AuthViewModel
import com.example.learningcurve.chatbot.ChatViewModel
import com.example.learningcurve.ui.theme.LearningCurveTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install splash screen
        installSplashScreen()
        FirebaseApp.initializeApp(this)
        // Set the content of the activity
        setContent {
            LearningCurveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Request microphone permission
                    RequestMicrophonePermission {
                        Log.d(TAG, "Microphone permission granted")
                    }
                    // Your main UI composable
                    MyApplication(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = AuthViewModel(),
                        chatViewModel = ChatViewModel()
                    )
                }
            }
        }
    }

    // Function to request microphone permission
    @Composable
    fun RequestMicrophonePermission(onPermissionGranted: () -> Unit) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    onPermissionGranted()
                    Log.d(TAG, "Microphone permission granted")
                } else {
                    Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Microphone permission denied")
                }
            }
        )

        // Request permission
        LaunchedEffect(Unit) {
            launcher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }
}
