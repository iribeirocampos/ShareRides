package com.example.sharist.data.local.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

enum class NetworkType {
    WIFI,
    MOBILE,
    NONE
}

object NetworkManager {

    fun getNetworkType(context: Context): NetworkType {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
            val capabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
                else -> NetworkType.NONE
            }
        } catch (e: SecurityException) {
            NetworkType.NONE
        }
    }
}