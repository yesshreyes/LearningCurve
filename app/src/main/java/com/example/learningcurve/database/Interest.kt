package com.example.learningcurve.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore


val db = Firebase.firestore

fun getUserInterests(userId: String, onResult: (List<String>) -> Unit) {
    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            val interests = document.get("interests") as? List<String> ?: emptyList()
            onResult(interests)
        }
}

//fun getRecommendedCourses(interests: List<String>, onResult: (List<Course>) -> Unit) {
//    val db = Firebase.firestore
//    db.collection("courses").get()
//        .addOnSuccessListener { result ->
//            val courses = result.documents.mapNotNull { document ->
//                document.toObject(Course::class.java)?.let { course ->
//                    if (interests.contains(course.category)) course else null
//                }
//            }
//            onResult(courses)
//        }
//        .addOnFailureListener { e ->
//            Log.e("Firestore", "Error fetching courses", e)
//            onResult(emptyList()) // Return an empty list in case of failure
//        }
//}

fun saveUserInterests(userId: String, interests: List<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = Firebase.firestore
    db.collection("users").document(userId)
        .set(mapOf("interests" to interests))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

fun saveUserFeedback(userId: String, courseId: String) {
    val db = Firebase.firestore
    db.collection("users").document(userId)
        .update("liked_courses", FieldValue.arrayUnion(courseId))
}
