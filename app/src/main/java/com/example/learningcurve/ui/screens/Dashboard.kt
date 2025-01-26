package com.example.learningcurve.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.learningcurve.auth.AuthState
import com.example.learningcurve.auth.AuthViewModel
import com.example.learningcurve.chatbot.ChatViewModel

@Composable
fun Dashboard(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel,chatViewModel: ChatViewModel) {

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xD9E3F2FF), Color(0x99FFFFFF))
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Hi, Shreyas", style = MaterialTheme.typography.displayMedium.copy(color = Color(0xFF1A73E8)))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "What would you like to learn today?", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1976D2)))
                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = {
                    authViewModel.signout()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White)) {
                    Text(text = "Sign out")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        containerColor = Color(0xCCFFFFFF),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = true, // Replace with dynamic selection logic
            onClick = { navController.navigate("dashboard")}
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Menu, contentDescription = "ChatBot") },
            label = { Text("ChatBot") },
            selected = false, // Replace with dynamic selection logic
            onClick = { navController.navigate("chatbot") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false, // Replace with dynamic selection logic
            onClick = { /* Navigate to Settings */ }
        )
    }
}

