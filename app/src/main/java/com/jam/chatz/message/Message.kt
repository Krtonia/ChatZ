package com.jam.chatz.message

import com.google.firebase.Timestamp

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.Companion.now(),
    val isRead: Boolean = false
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", Timestamp.Companion.now(), false)
}