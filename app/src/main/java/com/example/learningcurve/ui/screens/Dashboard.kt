package com.example.learningcurve.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.learningcurve.R
import com.example.learningcurve.auth.AuthState
import com.example.learningcurve.auth.AuthViewModel
import com.example.learningcurve.chatbot.ChatViewModel
import com.example.learningcurve.database.Course
import com.example.learningcurve.firebase.UploadButton
import com.example.learningcurve.ui.components.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore

fun getRecommendedCourses(interests: List<String>, onResult: (List<Course>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    if (interests.isEmpty()) {
        Log.d("Dashboard", "Interests list is empty, returning no recommended courses.")
        onResult(emptyList())
        return
    }

    firestore.collection("courses").get()
        .addOnSuccessListener { querySnapshot ->
            val recommendedCourses = querySnapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val title = doc.getString("Course Name") ?: return@mapNotNull null
                val category = doc.getString("Skills") ?: "Unknown"
                val platform = doc.getString("University") ?: "Unknown"
                val url = doc.getString("Course URL") ?: ""

                Log.d("Dashboard", "Fetched course: ID=$id, Title=$title, Category=$category, Platform=$platform, URL=$url")

                if (interests.any { category.contains(it, ignoreCase = true) }) {
                    Course(id, title, category, platform, url)
                } else null
            }

            Log.d("Dashboard", "Recommended courses after filtering: ${recommendedCourses.map { it.title }}")

            onResult(recommendedCourses)
        }
        .addOnFailureListener { exception ->
            Log.e("Dashboard", "Error fetching courses from Firestore", exception)
            onResult(emptyList())
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userId = authViewModel.getUserId() ?: return
    val authState = authViewModel.authState.observeAsState()
//    val name = authViewModel.getName(userId) ?: return

    val courses = remember { mutableStateOf<List<Course>>(emptyList()) }
    val interests = remember { mutableStateOf<List<String>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }  // Loading state

    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                interests.value = documentSnapshot.get("interests") as? List<String> ?: emptyList()
                Log.d("Dashboard", "Fetched user interests: ${interests.value}")
            }
    }

    LaunchedEffect(interests.value) {
        if (interests.value.isNotEmpty()) {
            getRecommendedCourses(interests.value) { recommendedCourses ->
                courses.value = recommendedCourses.filter { course ->
                    interests.value.any { interest ->
                        course.category.contains(interest, ignoreCase = true)
                    }
                }
                isLoading.value = false  // Set loading to false when data is fetched
            }
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Add logo image before the text
                        Image(
                            painter = painterResource(id = R.drawable.lclogo), // Replace with your logo resource
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(30.dp) // Set the size of your logo
                                .clip(CircleShape), // Make the image circular
                            contentScale = ContentScale.Crop // Crop/Zoom the image to fill the circular shape
                        )

                        Spacer(modifier = Modifier.width(8.dp)) // Add some spacing between logo and text
                        Text(
                            text = " Learning Curve",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic, // Make the text italic
                            fontFamily = FontFamily.Serif
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF1A3D7C))
            )

        },
        bottomBar = {
            BottomNavigationBar(navController, selectedRoute = "dashboard")
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(colors = listOf(Color(0xD9E3F2FF), Color(0x99FFFFFF)))),

            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                Text("Hi, Shreyas!", fontSize = 24.sp, color = Color(0xFF1A73E8), modifier = Modifier.padding(16.dp).align(Alignment.Start))
//                Spacer(modifier = Modifier.height(.dp))
                Text(
                    "What would you like to learn today?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold, // Makes text bold
                    color = Color(0xFF1976D2),
                    modifier = Modifier
                        .padding(top = 20.dp, start = 16.dp)
                        .align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Horizontally scrollable chips row
                if (interests.value.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interests.value.forEach { interest ->
                            Chip(label = interest) {
                                // Remove interest logic
                                authViewModel.removeInterest(userId, interest)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Show loading indicator while fetching courses
                if (isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.CenterHorizontally),
                        color = Color(0xFF1A73E8)
                    )
                } else {
                    // Display recommended courses
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(courses.value) { course ->
                            CourseCard(course)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F25)) // Deep Blue Black
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF102C57), Color(0xFF1A3D7C)) // Dark Blue Gradient
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section with gradient background and icon
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF145DA0), Color(0xFF0A2647)) // Lighter to Darker Blue
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School, // Replace with relevant icon
                    contentDescription = "Course Icon",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier.fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    Text(
                        text = course.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = course.platform,
                        fontSize = 12.sp,
                        color = Color(0xFFB0C4DE) // Light Blue-Grey
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (course.url.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.url))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Course link not available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E86C1)) // Cool Blue Button
                    ) {
                        Text("View Course", color = Color.White)
                    }
                }
            }
        }
    }
}
