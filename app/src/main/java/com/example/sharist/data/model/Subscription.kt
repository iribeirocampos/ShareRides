package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Serializable
@Entity(
    tableName = "subscription",
    indices = [
        Index(
            value = ["rideId", "userRiderId"],
            unique = true
        )
    ]
)
data class Subscription (
    @PrimaryKey val id:String,
    val rideId:String,
    val userRiderId:String,
    val onlyInRain: Boolean,
    // true when the rider subscribed to a recurring (weekly/daily) ride. The
    // subscription persists across the ride's date rolls; this flag is metadata
    // marking that the rider is here for the series, not a single occurrence.
    val reoccurrence: Boolean = false,
    val syncState: String? = SyncState.PENDING_CREATE.toString()
    ){
}