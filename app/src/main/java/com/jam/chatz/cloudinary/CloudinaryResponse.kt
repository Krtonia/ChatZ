package com.jam.chatz.cloudinary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudinaryResponse(
    @SerialName("public_id")
    val publicId: String? = null,

    @SerialName("version")
    val version: Long? = null,

    @SerialName("signature")
    val signature: String? = null,

    @SerialName("width")
    val width: Int? = null,

    @SerialName("height")
    val height: Int? = null,

    @SerialName("format")
    val format: String? = null,

    @SerialName("resource_type")
    val resourceType: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("tags")
    val tags: List<String>? = null,

    @SerialName("bytes")
    val bytes: Int? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("etag")
    val etag: String? = null,

    @SerialName("placeholder")
    val placeholder: Boolean? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("secure_url")
    val secureUrl: String? = null,

    @SerialName("original_filename")
    val originalFilename: String? = null
)