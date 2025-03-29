package com.jam.chatz.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jam.chatz.user.User
import com.jam.chatz.message.Message

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    // Get chat messages between current user and selected user
    fun getMessages(otherUserId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()

        // Create a unique chat ID using both user IDs (sorted to ensure consistency)
        val chatId = getChatId(currentUserId, otherUserId)

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                messagesLiveData.value = messages
            }

        return messagesLiveData
    }

    // Send a new message
    fun sendMessage(receiverId: String, messageText: String, callback: (Boolean) -> Unit) {
        if (messageText.isBlank() || currentUserId.isEmpty()) {
            callback(false)
            return
        }

        val chatId = getChatId(currentUserId, receiverId)
        val messageId = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document().id

        val message = Message(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = receiverId,
            message = messageText,
            timestamp = Timestamp.Companion.now(),
            isRead = false
        )

        // Save message
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                // Also update last message in chat metadata
                updateChatMetadata(chatId, currentUserId, receiverId, messageText)
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Update chat metadata for both users
    private fun updateChatMetadata(chatId: String, senderId: String, receiverId: String, lastMessage: String) {
        val timestamp = Timestamp.Companion.now()

        // Get user data for display in chat list
        firestore.collection("Users").document(receiverId).get()
            .addOnSuccessListener { receiverDoc ->
                val receiver = receiverDoc.toObject(User::class.java)

                // Chat metadata for sender
                val senderChatMetadata = hashMapOf(
                    "chatId" to chatId,
                    "userId" to receiverId,
                    "username" to (receiver?.username ?: ""),
                    "imageUrl" to (receiver?.imageurl ?: ""),
                    "lastMessage" to lastMessage,
                    "timestamp" to timestamp,
                    "unreadCount" to 0
                )

                firestore.collection("Users")
                    .document(senderId)
                    .collection("chats")
                    .document(receiverId)
                    .set(senderChatMetadata)
            }

        firestore.collection("Users").document(senderId).get()
            .addOnSuccessListener { senderDoc ->
                val sender = senderDoc.toObject(User::class.java)

                // Chat metadata for receiver
                val receiverChatMetadata = hashMapOf(
                    "chatId" to chatId,
                    "userId" to senderId,
                    "username" to (sender?.username ?: ""),
                    "imageUrl" to (sender?.imageurl ?: ""),
                    "lastMessage" to lastMessage,
                    "timestamp" to timestamp,
                    "unreadCount" to 1  // Increment unread for receiver
                )

                firestore.collection("Users")
                    .document(receiverId)
                    .collection("chats")
                    .document(senderId)
                    .set(receiverChatMetadata)
            }
    }

    // Helper to create consistent chat ID
    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) {
            "${uid1}_${uid2}"
        } else {
            "${uid2}_${uid1}"
        }
    }
}