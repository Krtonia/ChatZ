package com.jam.chatz.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jam.chatz.message.Message

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()


    fun getMessages(userId: String): LiveData<List<Message>> {
        return chatRepository.getMessages(userId)
    }


    fun sendMessage(receiverId: String, messageText: String, callback: (Boolean) -> Unit) {
        return chatRepository.sendMessage(receiverId, messageText, callback)
    }
}