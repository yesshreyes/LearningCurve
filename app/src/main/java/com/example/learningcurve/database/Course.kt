package com.example.learningcurve.database

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class Course(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val platform: String = "",
    val url: String = "" ,
//    val score: Int = 0
)

