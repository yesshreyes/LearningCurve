package com.example.learningcurve.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningcurve.auth.AuthViewModel
import com.example.learningcurve.ui.components.BottomNavigationBar
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val userId = authViewModel.getUserId() ?: return

    var interests by remember { mutableStateOf(listOf<String>()) }
    var newInterest by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }


    LaunchedEffect(userId) {
        authViewModel.getInterests(userId) { fetchedInterests ->
            interests = fetchedInterests
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text="Profile", color = Color.White, style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp))},
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF1A3D7C))
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, selectedRoute = "settings")
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        top = paddingValues.calculateTopPadding(),
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xD9E3F2FF), Color(0x99FFFFFF))
                        )
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileField(label = "Name", value = "Shreyas Deshmukh") { /* Update logic if needed */ }
                    ProfileField(label = "Email", value = "shreyas@gmail.com") { /* Update logic if needed */ }
                    ProfileField(label = "Password", value = "********") { /* Update logic if needed */ }
                    ProfileField(label = "Age", value = "21") { /* Update logic if needed */ }

                    // Interests Header
                    Text(
                        text = "Interests",
                        color = Color(0xFF1976D2),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Interests displayed in a FlowRow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        interests.forEach { interest ->
                            Chip(label = interest) {
                                // Remove interest logic
                                authViewModel.removeInterest(userId, interest)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Floating Action Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 130.dp, end = 30.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Color(0xFF1976D2))
                        .clickable { showDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Interest",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Dialog for adding interest
            if (showDialog) {
                AddInterestDialog(
                    onDismiss = { showDialog = false },
                    onAdd = {
                        if (newInterest.isNotEmpty()) {
                            Log.d("AddInterest", "Adding interest: $newInterest")
                            authViewModel.addInterest(userId, newInterest)
                            interests = interests + newInterest // Update the local list
                            newInterest = ""
                        }
                        showDialog = false
                    }
                    ,
                    newInterest = newInterest,
                    onValueChange = { newInterest = it }
                )
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                disabledLabelColor = Color.Gray,
                disabledTextColor = Color.Gray
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun Chip(label: String, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(end = 8.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Interest",
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRemove) // When the icon is clicked, this function is called
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInterestDialog(
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    newInterest: String,
    onValueChange: (String) -> Unit
) {
    // Using Material 3 Dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Interest",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Enter interest:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))  // Adding space between text and text field
                // Material 3 TextField
                TextField(
                    value = newInterest,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), // Padding for text field
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onAdd,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape = MaterialTheme.shapes.medium,  // Rounded corners from Material 3 shapes
        containerColor = MaterialTheme.colorScheme.surface, // Dialog background color
//        contentColor = MaterialTheme.colorScheme.onSurface, // Text color on surface
        modifier = Modifier.padding(16.dp) // Padding for the dialog content
    )
}
