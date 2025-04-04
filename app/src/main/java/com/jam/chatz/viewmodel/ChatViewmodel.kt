package com.jam.chatz.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jam.chatz.message.Message

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun getInitialMessages(userId: String, pageSize: Int): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val chatId = chatRepository.getChatId(FirebaseAuth.getInstance().currentUser?.uid ?: "", userId)

        chatRepository.getInitialMessages(chatId, pageSize) { messages, lastVisible ->
            messagesLiveData.value = messages
            lastVisibleDocument = lastVisible
        }

        return messagesLiveData
    }

    fun loadMoreMessages(
        userId: String,
        lastVisible: DocumentSnapshot,
        pageSize: Int
    ): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val chatId = chatRepository.getChatId(FirebaseAuth.getInstance().currentUser?.uid ?: "", userId)

        chatRepository.loadMoreMessages(chatId, lastVisible, pageSize) { messages, lastVisible ->
            messagesLiveData.value = messages
            lastVisibleDocument = lastVisible
        }

        return messagesLiveData
    }

    fun getMessages(userId: String): LiveData<List<Message>> {
        return chatRepository.getMessages(userId)
    }

    fun sendMessage(receiverId: String, messageText: String, callback: (Boolean) -> Unit) {
        chatRepository.sendMessage(receiverId, messageText) { success ->
            if (success) {
                // No need to manually update UI here - the real-time listener will handle it
            }
            callback(success)
        }
    }

    fun getLastVisibleDocument(): DocumentSnapshot? {
        return lastVisibleDocument
    }
}