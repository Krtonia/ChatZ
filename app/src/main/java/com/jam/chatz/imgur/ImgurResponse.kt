package com.jam.chatz.imgur

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImgurResponse(
    @SerialName("data")
    val data: ImgurData,

    @SerialName("success")
    val success: Boolean,

    @SerialName("status")
    val status: Int
)

@Serializable
data class ImgurData(
    @SerialName("id")
    val id: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("datetime")
    val datetime: Int? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("animated")
    val animated: Boolean? = null,

    @SerialName("width")
    val width: Int? = null,

    @SerialName("height")
    val height: Int? = null,

    @SerialName("size")
    val size: Int? = null,

    @SerialName("views")
    val views: Int? = null,

    @SerialName("bandwidth")
    val bandwidth: Long? = null,

    @SerialName("vote")
    val vote: String? = null,

    @SerialName("favorite")
    val favorite: Boolean? = null,

    @SerialName("section")
    val section: String? = null,

    @SerialName("account_url")
    val accountUrl: String? = null,

    @SerialName("account_id")
    val accountId: Int? = null,

    @SerialName("is_ad")
    val isAd: Boolean? = null,

    @SerialName("in_most_viral")
    val inMostViral: Boolean? = null,

    @SerialName("tags")
    val tags: List<String>? = null,

    @SerialName("ad_type")
    val adType: Int? = null,

    @SerialName("ad_url")
    val adUrl: String? = null,

    @SerialName("edited")
    val edited: String? = null,

    @SerialName("in_gallery")
    val inGallery: Boolean? = null,

    @SerialName("deletehash")
    val deletehash: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("link")
    val link: String? = null


)