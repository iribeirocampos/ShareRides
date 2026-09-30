package com.example.sharist.data.model

import androidx.room.Embedded

data class SubscribedRide(
    @Embedded
    val ride: Ride,

    @Embedded(prefix = "subscription_")
    val subscription: Subscription
)