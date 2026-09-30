package com.example.sharist.data.local.datasource

import android.content.Context
import java.io.File
import java.net.URL

class FileStorageDataSource {

    // ----------------------------
    // SAVE LOCAL FILE
    // ----------------------------
    fun saveImage(context: Context, bytes: ByteArray, fileName: String): String {
        val file = File(context.filesDir, fileName)
        file.writeBytes(bytes)
        return file.absolutePath
    }

    // ----------------------------
    // DELETE FILE
    // ----------------------------
    fun deleteImage(context: Context, path: String): Boolean {
        val file = resolveFile(context, path)
        return file.exists() && file.delete()
    }

    // ----------------------------
    // CHECK EXISTS
    // ----------------------------
    fun fileExists(context: Context, path: String): Boolean {
        val file = resolveFile(context, path)
        return file.exists()
    }

    // ----------------------------
    // 🔥 NEW: DOWNLOAD REMOTE IMAGE
    // ----------------------------
    fun downloadImageBytes(url: String): ByteArray {
        return URL(url)
            .openConnection()
            .apply {
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            .getInputStream()
            .use { it.readBytes() }
    }

    // ----------------------------
    // INTERNAL HELPERS
    // ----------------------------
    private fun resolveFile(context: Context, path: String): File {
        return if (File(path).isAbsolute) {
            File(path)
        } else {
            File(context.filesDir, path)
        }
    }
}