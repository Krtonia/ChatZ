package com.jam.chatz.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class UserRepo {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    fun getUsers(): LiveData<List<User>> {
        val usersLiveData = MutableLiveData<List<User>>()
        val currentUserId = auth.currentUser?.uid ?: ""

        firestore.collection("Users")  // Ensure collection name matches exactly
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = querySnapshot.documents.mapNotNull { document ->
                    val user = document.toObject(User::class.java)
                    // Exclude current user if needed
                    if (user?.userid != currentUserId) user else null
                }
                usersLiveData.value = userList
            }
            .addOnFailureListener { exception ->
                // Log the error or handle it appropriately
                usersLiveData.value = emptyList()
            }

        return usersLiveData
    }
}