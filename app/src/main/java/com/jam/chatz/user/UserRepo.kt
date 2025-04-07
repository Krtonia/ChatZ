package com.jam.chatz.user

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import java.lang.Exception

class UserRepo {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUsersWithLastMessage(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val allUsers =
                firestore.collection("Users").whereNotEqualTo("userid", currentUserId).get().await()
                    .toObjects(User::class.java).associateBy { it.userid }

            val chatDocs = firestore.collection("Users").document(currentUserId).collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING).get().await().documents

            val usersWithChats = chatDocs.mapNotNull { chatDoc ->
                val otherUserId = chatDoc.id
                val lastMessage = chatDoc.getString("lastMessage") ?: ""
                val timestamp = chatDoc.getTimestamp("timestamp")

                allUsers[otherUserId]?.copy(
                    lastMessage = lastMessage, lastMessageTimestamp = timestamp
                )
            }.toMutableList()

            val usersWithoutChats = allUsers.values.filter { user ->
                usersWithChats.none { it.userid == user.userid }
            }.map { it.copy(lastMessage = "")}

            usersWithChats + usersWithoutChats
        } catch (e: Exception) {
            Log.e("UserRepo", "Error fetching users", e)
            emptyList()
        }
    }
}