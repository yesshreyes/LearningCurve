package com.example.learningcurve.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learningcurve.R
import com.example.learningcurve.chatbot.ChatViewModel
import com.example.learningcurve.chatbot.MessageModel
import com.example.learningcurve.ui.components.BottomNavigationBar
import java.util.Locale

// Colors for the theme
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1D1F33), Color(0xFF1A3D7C))
)

val GeminiPurple = Color(0xFF6A4DDC)
val GeminiBlue = Color(0xFF4D8DDC)
val NeonGreen = Color(0xFF39FF14)
val NeonPink = Color(0xFFFF1493)
val MessageBubbleGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF4D8DDC), Color(0xFF6A4DDC)) // Use GeminiBlue and GeminiPurple
)
val ColorUserMessage = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1E90FF), Color.Black) // Use GeminiBlue and GeminiPurple
)

@Composable
fun ChatPage(modifier: Modifier = Modifier, navController: NavController, viewModel: ChatViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient) // Apply the gradient background
    ) {
        AppHeader()
        MessageList(modifier = Modifier.weight(1f), messageList = viewModel.messageList)
        MessageInput(
            onMessageSend = {
                viewModel.sendMessage(it)
            }
        )
        BottomNavigationBar(navController, selectedRoute = "chatbot")
    }
}

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGradient) // Apply the gradient background
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.bg), // Replace with your background image resource
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop, // Adjust how the image is scaled
            modifier = Modifier.fillMaxSize() // Fill the entire available space
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)) // Adjust the alpha for darkness
        )

        if (messageList.isEmpty()) {
            // Display a colorful empty state with a gradient background
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundGradient), // Apply the background gradient
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF1A3D7C), Color(0xFF1A3D7C)),
                                radius = 200f
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(16.dp),
                    painter = painterResource(id = R.drawable.baseline_question_answer_24),
                    contentDescription = "Question Icon",
                    tint = Color.White
                )
                Text(
                    "Ask me anything..",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else {
            // Display the message list with reverse layout for a chat-like experience
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Transparent), // Make LazyColumn background transparent
                reverseLayout = true
            ) {
                items(messageList.reversed()) {
                    MessageRow(messageModel = it)
                }
            }
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"
    val context = LocalContext.current // Use this inside the composable to get the context

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(
                        start = if (isModel) 16.dp else 70.dp,
                        end = if (isModel) 70.dp else 16.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        brush = if (isModel) MessageBubbleGradient else ColorUserMessage
                    )
                    .padding(16.dp)
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
            ) {
                Column {
                    SelectionContainer {
                        // Using AnnotatedString for clickable links
                        val annotatedMessage = buildAnnotatedString {
                            // Check for URL in the message and make it clickable
                            val regex = "(https?://[\\w-]+(\\.[\\w-]+)+(/\\S*)?)".toRegex()
                            val matches = regex.findAll(messageModel.message)

                            var lastEnd = 0
                            matches.forEach { match ->
                                append(messageModel.message.substring(lastEnd, match.range.first))
                                pushStringAnnotation(
                                    tag = "URL",
                                    annotation = match.value
                                )
                                withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                                    append(match.value)
                                }
                                pop()
                                lastEnd = match.range.last + 1
                            }
                            append(messageModel.message.substring(lastEnd))
                        }

                        ClickableText(
                            text = annotatedMessage,
                            onClick = { offset ->
                                annotatedMessage.getStringAnnotations("URL", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        // Ensure that this happens within the composable lifecycle
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        context.startActivity(intent) // This is fine now within the composable context
                                    }
                            },
                            style = androidx.compose.ui.text.TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(GeminiPurple, GeminiBlue)))
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(35.dp) // Set the size of the box for the icon
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.gemini), // Replace with your icon resource
                    contentDescription = "Gemini Icon",
                    modifier = Modifier
                        .fillMaxSize() // Make the icon fill the box
                )
            }
            Text(
                " Learning Curve Gemini",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

// Enhancements to the MessageInput with modern styling and gradient icons
@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showToast by remember { mutableStateOf(false) }

    // Move the speech recognition logic inside this composable
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val recognizedSpeech = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            message = recognizedSpeech?.get(0) ?: "No speech detected."
            onMessageSend(message)
        } else {
            showToast = true
        }
    }

    if (showToast) {
        Toast.makeText(context, "Speech recognition failed", Toast.LENGTH_SHORT).show()
        showToast = false
    }

    Row(
        modifier = Modifier
            .padding(12.dp)
            .background(BackgroundGradient)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, BackgroundGradient, RoundedCornerShape(12.dp))
                .shadow(5.dp, RoundedCornerShape(12.dp))
                .padding(8.dp),
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Type a message...", color = Color.Gray) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
        )

        IconButton(
            onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Go on then, say something.")
                }
                launcher.launch(intent)  // Start the speech recognition activity
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_mic_24),
                contentDescription = "Mic",
                tint = Color(0xFF4D8DDC),
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black, shape = CircleShape)
                    .padding(6.dp)
            )
        }

        IconButton(onClick = {
            if (message.isNotEmpty()) {
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color(0xFF6A4DDC),
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black, shape = CircleShape)
                    .padding(6.dp)
            )
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun PreviewChatPage() {
//    val dummyMessages = listOf(
//        MessageModel("Hello!", "user"),
//        MessageModel("Hi there! How can I assist you today?", "model")
//    )
//    ChatPage(viewModel = object : ChatViewModel() {
//        override val messageList: List<MessageModel> = dummyMessages
//    }, navController = NavController(LocalContext.current))
//}
