package com.example.sharist.data.local.util

import android.annotation.SuppressLint
import android.content.Context
import com.example.sharist.data.model.SimpleLocation
import java.util.UUID


class LocationProvider(private val context: Context) {

    object DefaultLocation {
        val VALUE = SimpleLocation(
            id = UUID.randomUUID().toString(),
            address = "Taguspark",
            latitude = 38.7169,
            longitude = -9.1399
        )
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): SimpleLocation {
        return try {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val location = locationManager.getLastKnownLocation(
                android.location.LocationManager.GPS_PROVIDER
            ) ?: return DefaultLocation.VALUE
            SimpleLocation(
                id = UUID.randomUUID().toString(),
                address = "Current location",
                latitude = location.latitude,
                longitude = location.longitude
            )
        } catch (e: Exception) {
            DefaultLocation.VALUE
        }
    }
}