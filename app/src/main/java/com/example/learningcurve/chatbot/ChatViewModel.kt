package com.example.learningcurve.chatbot

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningcurve.database.Course
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = Constants.apiKey
    )

    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                // Start chat with message history
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }
                )

                // Add user message
                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing...", "model")) // Temporary loading message

                // Send the user's message to the chat model
                val response = chat.sendMessage(question)
                messageList.removeAt(messageList.size - 1) // Remove "Typing..." message

                // Remove formatting
                val formattedResponse = response.text.toString().replace("*", "")

                // Add model response to chat history
                messageList.add(MessageModel(formattedResponse, "model"))

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error: ${e.message}", e)
                messageList.removeAt(messageList.size - 1) // Remove "Typing..." message
                messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }
    }

    fun getRecommendedCourses(interests: String, onResult: (List<Course>) -> Unit) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }
                )

                val userPrompt = "I am interested in $interests. Recommend me online courses in JSON format with fields: id, title, category, description, and url."

                // Add user input to chat history
                messageList.add(MessageModel(userPrompt, "user"))
                messageList.add(MessageModel("Fetching recommendations...", "model")) // Temporary message

                // Get AI response
                val response = chat.sendMessage(userPrompt)
                messageList.removeAt(messageList.size - 1) // Remove temporary message

                val responseText = response.text.toString()
                val formattedResponse = responseText.replace("*", "") // Remove markdown formatting

                // Parse response into Course list
                val courses = parseCourses(formattedResponse)

                // Add a generic success message to the chat
                messageList.add(MessageModel("Here are some recommended courses for you!", "model"))

                // Pass courses to the UI
                onResult(courses)

            } catch (e: Exception) {
                messageList.removeAt(messageList.size - 1)
                messageList.add(MessageModel("Error fetching courses: ${e.message}", "model"))
            }
        }
    }

    private fun parseCourses(responseText: String): List<Course> {
        val courses = mutableListOf<Course>()
        val courseRegex = Regex("""id:\s*(.*?)\nTitle:\s*(.*?)\nCategory:\s*(.*?)\nDescription:\s*(.*?)\nURL:\s*(.*?)\n""")
        val matches = courseRegex.findAll(responseText)

        for (match in matches) {
            val (id, title, category, platform, url) = match.destructured
            courses.add(Course(id = id, title = title, category = category, platform = platform, url = url))
        }

        return courses
    }
}

