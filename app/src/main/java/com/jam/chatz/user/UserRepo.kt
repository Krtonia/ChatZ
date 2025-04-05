package com.jam.chatz.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.lang.Exception

class UserRepo {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUsersWithLastMessage(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val chats = firestore.collection("Users")
                .document(currentUserId)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents

            val users = chats.mapNotNull { chatDoc ->
                val otherUserId = chatDoc.id
                val lastMessage = chatDoc.getString("lastMessage")
                val timestamp = chatDoc.getTimestamp("timestamp")

                // Fetch user details
                val user = firestore.collection("Users")
                    .document(otherUserId)
                    .get()
                    .await()
                    .toObject<User>()
                    ?.copy(
                        lastMessage = lastMessage ?: "No messages yet",
                        lastMessageTimestamp = timestamp
                    )
                user
            }

            users
        } catch (e: Exception) {
            Log.e("UserRepo", "Error fetching messages", e)
            emptyList()
        }
    }
}