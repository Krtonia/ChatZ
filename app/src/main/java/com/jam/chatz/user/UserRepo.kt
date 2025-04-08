package com.jam.chatz.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception

class UserRepo {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    interface UserRepoCallback {
        fun onSuccess(users: List<User>)
        fun onFailure(exception: Exception)
    }

    fun getUsersWithLastMessage(callback: UserRepoCallback) {
        val currentUserId = auth.currentUser?.uid ?: run {
            callback.onFailure(Exception("User not authenticated"))
            return
        }

        // Fetch all users excluding the current user
        firestore.collection("Users")
            .whereNotEqualTo("userid", currentUserId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val allUsers = task.result?.toObjects(User::class.java)
                        ?.associateBy { it.userid } ?: emptyMap()

                    // Fetch the chats for the current user
                    firestore.collection("Users").document(currentUserId).collection("chats")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .addOnCompleteListener { chatTask ->
                            if (chatTask.isSuccessful) {
                                val chatDocs: QuerySnapshot? = chatTask.result
                                val usersWithChats = chatDocs?.mapNotNull { chatDoc ->
                                    val otherUserId = chatDoc.id
                                    val lastMessage = chatDoc.getString("lastMessage") ?: ""
                                    val timestamp = chatDoc.getTimestamp("timestamp")

                                    // Populate the user with last message details
                                    allUsers[otherUserId]?.copy(
                                        lastMessage = lastMessage,
                                        lastMessageTimestamp = timestamp
                                    )
                                }?.toMutableList() ?: mutableListOf()

                                // Add users without chats (copy with empty lastMessage)
                                val usersWithoutChats = allUsers.values.filter { user ->
                                    usersWithChats.none { it.userid == user.userid }
                                }.map { it.copy(lastMessage = "") }

                                // Combine the users with and without chats
                                callback.onSuccess(usersWithChats + usersWithoutChats)
                            } else {
                                callback.onFailure(
                                    chatTask.exception ?: Exception("Error fetching chats")
                                )
                            }
                        }
                } else {
                    callback.onFailure(task.exception ?: Exception("Error fetching users"))
                }
            }
    }
}