package com.example.learningcurve.firebase

import android.content.Context
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import org.json.JSONArray


fun cleanAndUploadJson(jsonString: String) {
    try {
        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()

        // Parse the JSON string as a JSONArray
        val jsonArray = JSONArray(jsonString)

        // Iterate over each item in the JSONArray
        for (i in 0 until jsonArray.length()) {
            val courseObject = jsonArray.getJSONObject(i)

            // Extract individual fields from the JSONObject
            val courseName = courseObject.optString("Course Name")
            val university = courseObject.optString("University")
            val courseUrl = courseObject.optString("Course URL")
            val skills = courseObject.optString("Skills")

            // Create a Map to store the course data
            val courseData = hashMapOf(
                "Course Name" to courseName,
                "University" to university,
                "Course URL" to courseUrl,
                "Skills" to skills
            )

            // Upload the data to Firestore
            db.collection("courses")
                .add(courseData)
                .addOnSuccessListener { documentReference ->
                    // Success: You can log or show a toast here
                    Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    // Failure: Log or show the error
                    Log.w("Firestore", "Error adding document", e)
                }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


@Composable
fun UploadButton(context: Context) {
    Button(onClick = {
        // Load your JSON file
        val jsonString: String = context.assets.open("courses.json").bufferedReader().use { it.readText() }

        // Call the function to clean and upload the JSON data
        cleanAndUploadJson(jsonString)
    }) {
        Text("Upload Course Data to Firestore")
    }
}
