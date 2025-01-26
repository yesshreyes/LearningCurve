package com.example.learningcurve

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learningcurve.auth.AuthViewModel
import com.example.learningcurve.ui.screens.LoginPage
import com.example.learningcurve.ui.screens.SignupPage
import com.example.learningcurve.ui.screens.ChatPage
import com.example.learningcurve.chatbot.ChatViewModel
import com.example.learningcurve.ui.screens.Dashboard

@Composable
fun MyApplication(modifier: Modifier = Modifier,authViewModel: AuthViewModel,chatViewModel: ChatViewModel) {
    val navController = rememberNavController()
//    val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier,navController,authViewModel)
        }
        composable("signup"){
            SignupPage(modifier,navController,authViewModel)
        }
        composable("dashboard"){
            Dashboard(modifier,navController,authViewModel,chatViewModel)
        }
        composable("chatbot"){
            ChatPage(modifier,navController,chatViewModel)
        }
    })
}