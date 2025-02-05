//package com.example.learningcurve.database
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//
//class InterestViewModel : ViewModel() {
//    // Dummy user interests list (can be replaced with actual data persistence)
//    var interests by mutableStateOf(mutableListOf("Football", "Music", "Traveling"))
//        private set
//
//    // Function to get user interests (this can be fetching from a backend or local storage)
//    fun getUserInterests(): List<String> {
//        return interests
//    }
//
//    // Function to update the interests
//    fun updateUserInterests(newInterest: String) {
//        if (newInterest.isNotEmpty() && !interests.contains(newInterest)) {
//            interests = interests.toMutableList().apply { add(newInterest) }
//        }
//    }
//
//    // Function to remove an interest
//    fun removeInterest(interest: String) {
//        interests = interests.toMutableList().apply { remove(interest) }
//    }
//}
