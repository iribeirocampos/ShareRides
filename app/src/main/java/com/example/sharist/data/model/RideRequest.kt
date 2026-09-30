package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
@Entity(tableName = "ride_request")
data class RideRequest(
    @PrimaryKey val id: String,
    val userId:String,
    val departure: SimpleLocation,
    val destination: SimpleLocation,
    val toleranceRange:Int,
    val date: Instant,
    val isWeekly:Boolean,
    val isDaily: Boolean,
    val driverId:String?=null,
    // For recurring (weekly/daily) requests: when false the request stops rolling
    // forward and simply ages out of the upcoming (date > now) queries.
    val active: Boolean = true,
    val syncState: String = SyncState.PENDING_CREATE.toString()
)