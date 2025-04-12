package com.jam.chatz.message

import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    @Contextual val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val isRead: Boolean = false,
    val imageUrl: String? = null,  // This stores the image URL
    val isImage: Boolean = true   // This indicates if it's an image message
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", Timestamp.now(), false, null, false)
}