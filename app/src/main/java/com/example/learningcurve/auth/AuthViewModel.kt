package com.example.learningcurve.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learningcurve.database.Course
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email : String,password : String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signup(email : String,password : String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
    fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    fun getName(userId: String, onResult: (String?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the document in the 'users' collection with the document ID as userId
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                // Get the name field from the document
                val name = documentSnapshot.getString("name")

                // Call the result callback with the name
                onResult(name)
            }
            .addOnFailureListener { exception ->
                // Handle any error that occurs while fetching the document
                Log.e("Firestore", "Error fetching user name", exception)
                onResult(null)
            }
    }

    fun setName(userId: String, name: String, onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the document in the 'users' collection with the document ID as userId
        firestore.collection("users").document(userId).set(
            hashMapOf("name" to name)  // This sets the 'name' field
        )
            .addOnSuccessListener {
                // Successfully set the name
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                // Handle any error that occurs while setting the name
                Log.e("Firestore", "Error setting user name", exception)
                onComplete(false)
            }
    }

    fun addInterest(userId: String, newInterest: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                userDocRef.update("interests", FieldValue.arrayUnion(newInterest))
            } else {
                userDocRef.set(mapOf("interests" to listOf(newInterest)))
            }
        }.addOnFailureListener {
            Log.e("AuthViewModel", "Error adding interest", it)
        }
    }

    fun getInterests(userId: String, onResult: (List<String>) -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val interests = documentSnapshot.get("interests") as? List<String> ?: emptyList()
                onResult(interests)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }

    }
    fun removeInterest(userId: String, interest: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.update("interests", FieldValue.arrayRemove(interest))
    }

    fun getUserProfile(userId: String, onResult: (Map<String, Any>) -> Unit) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val profile = mapOf(
                    "name" to (document.getString("name") ?: ""),
                    "age" to (document.getString("age") ?: ""),
                    "interests" to (document["interests"] as? List<String> ?: listOf())
                )
                onResult(profile)
            }
        }
    }


    fun updateProfile(userId: String, name: String, age: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.update(mapOf("name" to name, "age" to age))
    }


//    fun getRecommendedCourses(interests: List<String>, onResult: (List<Course>) -> Unit) {
//        if (interests.isEmpty()) {
//            Log.d("Dashboard", "Interests list is empty, returning no recommended courses.")
//            onResult(emptyList())
//            return
//        }
//
//        firestore.collection("courses").get()
//            .addOnSuccessListener { querySnapshot ->
//                val recommendedCourses = querySnapshot.documents.mapNotNull { doc ->
//                    val title = doc.getString("title") ?: return@mapNotNull null
//                    val category = doc.getString("category") ?: "Unknown"
//                    val description = doc.getString("description") ?: ""
//
//                    Log.d("Dashboard", "Fetched course: Title=$title, Category=$category, Description=$description")
//
//                    if (interests.any { category.contains(it, ignoreCase = true) }) {
//                        Course(title, category, description)
//                    } else null
//                }
//
//                Log.d("Dashboard", "Recommended courses before filtering: ${recommendedCourses.map { it.title }}")
//
//                onResult(recommendedCourses)
//            }
//            .addOnFailureListener { exception ->
//                Log.e("Dashboard", "Error fetching courses from Firestore", exception)
//                onResult(emptyList())
//            }
//    }

}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}
