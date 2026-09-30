package com.example.sharist.data.dto

import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.SubscribedRide
import com.example.sharist.data.model.Subscription
import kotlinx.serialization.Serializable


@Serializable
data class SubscribedRideDto(
    val id: String,
    val rideId: String,
    val userRiderId: String,
    val onlyInRain: Boolean,
    val ride: Ride
){

}

fun SubscribedRideDto.toDomain(): SubscribedRide {
    return SubscribedRide(
        ride = ride,
        subscription = Subscription(
            id = id,
            rideId = rideId,
            userRiderId = userRiderId,
            onlyInRain = onlyInRain
        )
    )
}