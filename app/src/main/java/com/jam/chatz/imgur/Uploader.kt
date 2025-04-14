package com.jam.chatz.imgur

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

object Uploader {
    private val imgurService = ApiInterface.create()

    suspend fun uploadImage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val byteArray = inputStream.readBytes()
            inputStream.close()

            val requestFile = RequestBody.create(
                "image/*".toMediaTypeOrNull(),
                byteArray
            )

            val imagePart = MultipartBody.Part.createFormData(
                "image",
                "chat_image_${System.currentTimeMillis()}.jpg",
                requestFile
            )

            val response = imgurService.uploadImage(imagePart)

            if (response.isSuccessful) {
                response.body()?.data?.link?.also {
                }
            } else {
                Log.e("Uploader", "Upload failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("Uploader", "Upload error", e)
            null
        }
    }
}