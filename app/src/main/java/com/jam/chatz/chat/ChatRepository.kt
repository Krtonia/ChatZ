package com.jam.chatz.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jam.chatz.user.User
import com.jam.chatz.message.Message

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    fun getInitialMessages(
        chatId: String, pageSize: Int, callback: (List<Message>, DocumentSnapshot?) -> Unit
    ) {
        firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(pageSize.toLong()).get()
            .addOnSuccessListener { querySnapshot ->
                val messages = querySnapshot.toObjects(Message::class.java).reversed()
                val lastVisible = if (!querySnapshot.isEmpty) {
                    querySnapshot.documents[querySnapshot.size() - 1]
                } else {
                    null
                }
                callback(messages, lastVisible)
            }.addOnFailureListener { e ->
                callback(emptyList(), null)
            }
    }

    fun loadMoreMessages(
        chatId: String,
        lastVisible: DocumentSnapshot,
        pageSize: Int,
        callback: (List<Message>, DocumentSnapshot?) -> Unit
    ) {
        firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible)
            .limit(pageSize.toLong()).get().addOnSuccessListener { querySnapshot ->
                val messages = querySnapshot.toObjects(Message::class.java).reversed()
                val newLastVisible = if (!querySnapshot.isEmpty) {
                    querySnapshot.documents[querySnapshot.size() - 1]
                } else {
                    null
                }
                callback(messages, newLastVisible)
            }.addOnFailureListener { e ->
                callback(emptyList(), null)
            }
    }

    fun getMessages(otherUserId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val chatId = getChatId(currentUserId, otherUserId)

        firestore.collection("chats").document(chatId).collection("messages")
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

    fun sendMessage(receiverId: String, messageText: String, callback: (Boolean) -> Unit) {
        if (messageText.isBlank() || currentUserId.isEmpty()) {
            callback(false)
            return
        }

        val chatId = getChatId(currentUserId, receiverId)
        val messageId =
            firestore.collection("chats").document(chatId).collection("messages").document().id

        val message = Message(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = receiverId,
            text = messageText,
            timestamp = Timestamp.now(),
            isRead = false
        )

        firestore.collection("chats").document(chatId).collection("messages").document(messageId)
            .set(message).addOnSuccessListener {
                updateChatMetadata(chatId, currentUserId, receiverId, messageText)
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
    }

    fun sendImageMessage(receiverId: String, imageUrl: String, callback: (Boolean) -> Unit) {
        if (imageUrl.isBlank() || currentUserId.isEmpty()) {
            callback(false)
            return
        }

        val chatId = getChatId(currentUserId, receiverId)
        val messageId =
            firestore.collection("chats").document(chatId).collection("messages").document().id

        val message = Message(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = receiverId,
            text = "",
            timestamp = Timestamp.now(),
            isRead = false,
            imageUrl = imageUrl,
            type = "image"
        )

        firestore.collection("chats").document(chatId).collection("messages").document(messageId)
            .set(message).addOnSuccessListener {
                updateChatMetadata(chatId, currentUserId, receiverId, "\uD83D\uDDBC\uFE0F Photo")
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
    }

    fun addMessage(chatId: String, message: Message, callback: (Boolean) -> Unit) {
        val messageData = hashMapOf(
            "messageId" to message.messageId,
            "senderId" to message.senderId,
            "receiverId" to message.receiverId,
            "text" to message.text,
            "timestamp" to message.timestamp,
            "isRead" to message.isRead,
            "imageUrl" to message.imageUrl,
            "type" to message.type
        )

        FirebaseFirestore.getInstance().collection("chats").document(chatId).collection("messages")
            .document(message.messageId).set(messageData).addOnSuccessListener {
                if (message.senderId.isNotEmpty() && message.receiverId.isNotEmpty()) {
                    val lastMessageText = if (message.isImage) "\uD83D\uDDBC\uFE0F Photo" else message.text
                    updateChatMetadata(
                        chatId, message.senderId, message.receiverId, lastMessageText
                    )
                }
                callback(true)
            }.addOnFailureListener { e ->
                Log.e("ChatRepository", "Error adding message ${e.message}")
                callback(false)
            }
    }

    private fun updateChatMetadata(
        chatId: String, senderId: String, receiverId: String, lastMessage: String
    ) {
        val timestamp = Timestamp.now()

        firestore.collection("Users").document(receiverId).get()
            .addOnSuccessListener { receiverDoc ->
                val receiver = receiverDoc.toObject(User::class.java)

                val senderChatMetadata = hashMapOf(
                    "chatId" to chatId,
                    "userId" to receiverId,
                    "username" to (receiver?.username ?: ""),
                    "imageUrl" to (receiver?.imageurl ?: ""),
                    "lastMessage" to lastMessage,
                    "timestamp" to timestamp,
                    "unreadCount" to 0
                )

                firestore.collection("Users").document(senderId).collection("chats")
                    .document(receiverId).set(senderChatMetadata)
            }

        firestore.collection("Users").document(senderId).get().addOnSuccessListener { senderDoc ->
            val sender = senderDoc.toObject(User::class.java)

            val receiverChatMetadata = hashMapOf(
                "chatId" to chatId,
                "userId" to senderId,
                "username" to (sender?.username ?: ""),
                "imageUrl" to (sender?.imageurl ?: ""),
                "lastMessage" to lastMessage,
                "timestamp" to timestamp,
                "unreadCount" to 1
            )

            firestore.collection("Users").document(receiverId).collection("chats")
                .document(senderId).set(receiverChatMetadata)
        }
    }

    fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) {
            "${uid1}_${uid2}"
        } else {
            "${uid2}_${uid1}"
        }
    }
}