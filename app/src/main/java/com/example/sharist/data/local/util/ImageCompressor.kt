package com.example.sharist.data.local.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File

object ImageCompressor {


    // In ImageCompressor
    fun compressImageFromFile(file: File): ByteArray {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)?: throw IllegalArgumentException("Unable to decode image file")
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }
    fun compressImage(
        context: Context,
        uri: Uri
    ): ByteArray {

        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val outputStream = ByteArrayOutputStream()

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            70,
            outputStream
        )

        inputStream?.close()

        return outputStream.toByteArray()
    }
}