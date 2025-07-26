package com.jam.chatz.message

import com.bumptech.glide.annotation.GlideModule
import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    @Contextual val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val imageUrl: String? = null,
    val type: String = "text",
) {
    val isImage: Boolean
        get() = type == "image"

    val message: String
        get() = text

    constructor() : this("", "", "", "", Timestamp.now(), false, null, "text")
}