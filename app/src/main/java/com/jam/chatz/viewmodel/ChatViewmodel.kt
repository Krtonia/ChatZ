package com.jam.chatz.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.chat.ChatRepository
import com.jam.chatz.message.Message

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val _isLoading = MutableLiveData(false)

    fun getInitialMessages(userId: String, pageSize: Int): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("ChatViewModel", "Current user ID is null")
            messagesLiveData.postValue(emptyList())
            return messagesLiveData
        }

        val chatId = chatRepository.getChatId(currentUserId, userId)
        _isLoading.postValue(true)

        chatRepository.getInitialMessages(chatId, pageSize) { messages, lastVisible ->
            _isLoading.postValue(false)
            messagesLiveData.postValue(messages)
            lastVisibleDocument = lastVisible
            if (messages.isEmpty()) {
                Log.d("ChatViewModel", "No initial messages found")
            }
        }

        return messagesLiveData
    }

    fun loadMoreMessages(
        userId: String,
        lastVisible: DocumentSnapshot,
        pageSize: Int
    ): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("ChatViewModel", "Current user ID is null")
            messagesLiveData.postValue(emptyList())
            return messagesLiveData
        }

        _isLoading.postValue(true)
        val chatId = chatRepository.getChatId(currentUserId, userId)

        chatRepository.loadMoreMessages(chatId, lastVisible, pageSize) { messages, lastVisible ->
            _isLoading.postValue(false)
            messagesLiveData.postValue(messages)
            lastVisibleDocument = lastVisible
            Log.d("ChatViewModel", "Loaded ${messages.size} more messages")
        }

        return messagesLiveData
    }

    fun getMessages(userId: String): LiveData<List<Message>> {
        return chatRepository.getMessages(userId)
    }

    fun sendImageMessage(receiverId: String, imageUrl: String, callback: (Boolean) -> Unit) {
        run {
            Log.e("ChatViewModel", "Cannot send message - user not authenticated")
            callback(false)
            return
        }

        chatRepository.sendImageMessage(receiverId, imageUrl) { success ->
            if (success) {
                Log.d("ChatViewModel", "Image message sent successfully")
            } else {
                Log.e("ChatViewModel", "Failed to send image message")
            }
            callback(success)
        }
    }

    fun sendMessageObject(receiverId: String, message: Message, callback: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            callback(false)
            return
        }

        val updatedMessage = message.copy(
            senderId = currentUserId,
            receiverId = receiverId,
            messageId = message.messageId.ifEmpty {
                FirebaseFirestore.getInstance().collection("chats")
                    .document(chatRepository.getChatId(currentUserId, receiverId))
                    .collection("messages").document().id
            }
        )

        val chatId = chatRepository.getChatId(currentUserId, receiverId)

        chatRepository.addMessage(chatId, updatedMessage) { success ->
            if (success) {
                Log.d("ChatViewModel", "Message sent successfully: ${updatedMessage.messageId}")
            } else {
                Log.e("ChatViewModel", "Failed to send message")
            }
            callback(success)
        }
    }

    fun sendMessage(receiverId: String, messageText: String, callback: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("ChatViewModel", "Cannot send message - user not authenticated")
            callback(false)
            return
        }

        chatRepository.sendMessage(receiverId, messageText) { success ->
            if (success) {
                Log.d("ChatViewModel", "Text message sent successfully")
            } else {
                Log.e("ChatViewModel", "Failed to send text message")
            }
            callback(success)
        }
    }

    fun getLastVisibleDocument(): DocumentSnapshot? {
        return lastVisibleDocument
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}