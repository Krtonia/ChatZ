package com.jam.chatz.user

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
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

        firestore.collection("Users").document(currentUserId).collection("chats")
            .orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnCompleteListener { chatTask ->
                if (chatTask.isSuccessful) {
                    val chatDocs = chatTask.result
                    val userIds = chatDocs?.map { it.id } ?: emptyList()

                    if (userIds.isEmpty()) {
                        callback.onSuccess(emptyList())
                        return@addOnCompleteListener
                    }

                    firestore.collection("Users").whereIn("userid", userIds).get()
                        .addOnCompleteListener { userTask ->
                            if (userTask.isSuccessful) {
                                val users =
                                    userTask.result?.toObjects(User::class.java) ?: emptyList()
                                val usersWithMessages = users.map { user ->
                                    val chatDoc = chatDocs.firstOrNull { it.id == user.userid }
                                    user.copy(
                                        lastMessage = chatDoc?.getString("lastMessage") ?: "",
                                        lastMessageTimestamp = chatDoc?.getTimestamp("timestamp")
                                    )
                                }
                                callback.onSuccess(usersWithMessages)
                            } else {
                                callback.onFailure(
                                    userTask.exception ?: Exception("Error fetching users")
                                )
                            }
                        }
                } else {
                    callback.onFailure(chatTask.exception ?: Exception("Error fetching chats"))
                }
            }
    }

    fun getAllUsers(callback: UserRepoCallback) {
        val currentUserId = auth.currentUser?.uid ?: run {
            callback.onFailure(Exception("User not authenticated"))
            return
        }
        firestore.collection("Users").whereNotEqualTo("userid", currentUserId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val users = task.result?.toObjects(User::class.java)?.map {
                        it.copy(lastMessage = "", lastMessageTimestamp = null)
                    } ?: emptyList()
                    callback.onSuccess(users)
                } else {
                    callback.onFailure(task.exception ?: Exception("Error fetching users"))
                }
            }
    }
}