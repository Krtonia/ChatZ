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
            // 1. Fetch all chats (sorted by most recent)
            val chats = firestore.collection("Users")
                .document(currentUserId)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents

            // 2. Fetch user details for each chat
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

    /*suspend fun getUsersWithChatMetadata(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        return try {

            val users = firestore.collection("Users")
                .whereNotEqualTo("userid", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject<User>() }

            val chats = firestore.collection("Users")
                .document(currentUserId)
                .collection("chats")
                .get()
                .await()
                .documents
                .associate { doc ->
                    doc.id to ChatMetadata(
                        lastMessage = doc.getString("lastMessage"),
                        timestamp = doc.getTimestamp("timestamp")
                    )
                }

            users.map { user ->
                val metadata = chats[user.userid]
                user.copy(
                    lastMessage = metadata?.lastMessage,
                    lastMessageTimestamp = metadata?.timestamp
                )
            }.sortedByDescending { it.lastMessageTimestamp?.seconds ?: 0 }

        } catch (e: Exception) {
            Log.e("UserRepo", "Error fetching users/chats", e)
            emptyList()
        }
    }*/

    private data class ChatMetadata(
        val lastMessage: String?,
        val timestamp: com.google.firebase.Timestamp?
    )
}

//class UserRepo {
//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth = Firebase.auth
//    private val current = auth.currentUser?.uid ?: ""
//
//    suspend fun getUsersWithChatMetadata(): List<User> {
//        val currentUserId = auth.currentUser?.uid ?: return emptyList()
//
//        val users = firestore.collection("Users").whereNotEqualTo("userid", currentUserId).get()
//            .await().documents.mapNotNull { it.toObject<User>() }
//    }
//
//    fun getUsers(): LiveData<List<User>> {
//        val usersLiveData = MutableLiveData<List<User>>()
//        val currentUserId = auth.currentUser?.uid ?: ""
//
//        firestore.collection("Users")
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                val userList = querySnapshot.documents.mapNotNull { document ->
//                    val user = document.toObject(User::class.java)
//                    if (user?.userid != currentUserId) user else null
//                }
//                usersLiveData.value = userList
//            }
//            .addOnFailureListener { exception ->
//                usersLiveData.value = emptyList()
//            }
//
//        return usersLiveData
//    }
//}