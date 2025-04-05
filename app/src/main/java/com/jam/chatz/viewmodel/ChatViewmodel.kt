package com.jam.chatz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.jam.chatz.chat.ChatRepository
import com.jam.chatz.message.Message

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private var lastVisibleDocument: DocumentSnapshot? = null

    fun getInitialMessages(userId: String, pageSize: Int): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val chatId =
            chatRepository.getChatId(FirebaseAuth.getInstance().currentUser?.uid ?: "", userId)

        chatRepository.getInitialMessages(chatId, pageSize) { messages, lastVisible ->
            messagesLiveData.value = messages
            lastVisibleDocument = lastVisible
        }

        return messagesLiveData
    }

    fun loadMoreMessages(
        userId: String, lastVisible: DocumentSnapshot, pageSize: Int
    ): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val chatId =
            chatRepository.getChatId(FirebaseAuth.getInstance().currentUser?.uid ?: "", userId)

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
            if (success)
            callback(success)
        }
    }

    fun getLastVisibleDocument(): DocumentSnapshot? {
        return lastVisibleDocument
    }
}