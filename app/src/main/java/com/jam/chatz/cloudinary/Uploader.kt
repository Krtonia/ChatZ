package com.jam.chatz.cloudinary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import androidx.core.graphics.scale
import com.jam.chatz.util.Constants.CLOUD_NAME
import com.jam.chatz.util.Constants.UPLOAD_PRESET

object Uploader {
    private val cloudinaryService = ApiInterface.create()
    private const val MAX_RETRIES = 3
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // set max size to 5mb
    private const val COMPRESSION_QUALITY = 85

    suspend fun uploadImage(context: Context, uri: Uri): String? {
        var lastException: Exception? = null

        // Retry
        repeat(MAX_RETRIES) { attempt ->
            try {
                Log.d("Uploader", "Upload attempt ${attempt + 1}")

                val compressedImageData = compressImage(context, uri)
                if (compressedImageData == null) {
                    Log.e("Uploader", "Failed to compress image")
                    return null
                }

                val requestFile = RequestBody.create(
                    "image/jpeg".toMediaTypeOrNull(),
                    compressedImageData
                )

                val imagePart = MultipartBody.Part.createFormData(
                    "file",
                    "profile_${System.currentTimeMillis()}_${Random.nextInt(1000)}.jpg",
                    requestFile
                )

                val uploadPresetBody = UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = cloudinaryService.uploadImage(CLOUD_NAME, imagePart, uploadPresetBody)
                Log.d("Uploader", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    var imageUrl = response.body()?.secureUrl ?: response.body()?.url
                    // Force HTTPS
                    imageUrl = imageUrl?.replace("http://", "https://")
                    Log.d("Uploader", "Upload successful: $imageUrl")
                    return imageUrl
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Uploader", "Upload failed: Code ${response.code()}, Body: $errorBody")

                    if (response.code() == 400) {
                        Log.e("Uploader", "Bad request error - not retrying")
                        return null
                    }

                    lastException = Exception("HTTP ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("Uploader", "Upload error on attempt ${attempt + 1}: ${e.message}")
                lastException = e

                if (attempt < MAX_RETRIES - 1) {
                    delay(1000L * (attempt + 1))
                }
            }
        }

        Log.e("Uploader", "All upload attempts failed. Last error: ${lastException?.message}")
        return null
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e("Uploader", "Failed to decode bitmap")
                return null
            }

            val maxWidth = 1920
            val maxHeight = 1920
            val scaledBitmap = scaleBitmap(originalBitmap, maxWidth, maxHeight)

            val outputStream = ByteArrayOutputStream()
            var quality = COMPRESSION_QUALITY

            do {
                outputStream.reset()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                quality -= 5
            } while (outputStream.size() > MAX_FILE_SIZE && quality > 10)

            if (outputStream.size() > MAX_FILE_SIZE) {
                Log.e("Uploader", "Image too large even after compression: ${outputStream.size()} bytes")
                return null
            }

            Log.d("Uploader", "Compressed image size: ${outputStream.size()} bytes")
            return outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("Uploader", "Error compressing image: ${e.message}")
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )

        return if (ratio < 1.0f) {
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            bitmap.scale(newWidth, newHeight)
        } else {
            bitmap
        }
    }
}