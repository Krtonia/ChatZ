package com.jam.chatz.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jam.chatz.message.Message

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()

    // Get messages for a specific chat
    fun getMessages(userId: String): LiveData<List<Message>> {
        return chatRepository.getMessages(userId)
    }

    // Send a new message
    fun sendMessage(receiverId: String, messageText: String, callback: (Boolean) -> Unit) {
        return chatRepository.sendMessage(receiverId, messageText, callback)
    }
}