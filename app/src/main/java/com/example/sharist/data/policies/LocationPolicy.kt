package com.example.sharist.data.policies

import com.example.sharist.data.local.util.NetworkType

object LocationPolicy {

    const val DEFAULT_CACHE_RADIUS_KM = 10.0
    const val DEFAULT_FETCH_RADIUS_KM = 15.0

    const val WIFI_CACHE_RADIUS_KM = 95.0
    const val WIFI_FETCH_RADIUS_KM = 100.0

    fun cacheRadius(network: NetworkType): Double =
        when (network) {
            NetworkType.WIFI -> WIFI_CACHE_RADIUS_KM
            NetworkType.MOBILE -> DEFAULT_CACHE_RADIUS_KM
            NetworkType.NONE -> 0.0
        }

    fun fetchRadius(network: NetworkType): Double =
        when (network) {
            NetworkType.WIFI -> WIFI_FETCH_RADIUS_KM
            NetworkType.MOBILE -> DEFAULT_FETCH_RADIUS_KM
            NetworkType.NONE -> 0.0
        }
}
