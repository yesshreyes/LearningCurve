package com.example.learningcurve.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController, selectedRoute: String) {
    NavigationBar(
        containerColor = Color(0xCCFFFFFF),
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("dashboard", Icons.Default.Home, "Dashboard"),
            Triple("chatbot", Icons.Default.Menu, "ChatBot"),
            Triple("settings", Icons.Default.Settings, "Settings")
        )

        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(label) },
                selected = selectedRoute == route,
                onClick = {
                    if (selectedRoute != route) {
                        navController.navigate(route)
                    }
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1976D2), // Blue shade
                    selectedTextColor = Color(0xFF1976D2),
                    indicatorColor = Color(0x221976D2) // **Very light transparent blue**
                )
            )
        }
    }
}


