package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant


@Serializable
@Entity(tableName = "ride")
data class Ride(
    @PrimaryKey val id: String,
    val departure: SimpleLocation,
    val destination: SimpleLocation,
    val departureDateTime: Instant?,
    val arrivalDateTime: Instant?,
    val cost: Double,
    val availableSeats: Int,
    val userId:String,
    val vehicleId:String,
    val cancelUpToDays : Int,
    val isWeekly:Boolean,
    val isDaily:Boolean,
    val syncState: String = SyncState.PENDING_CREATE.toString()
)


fun Ride.isCancellableNow(): Boolean {
    val departure = departureDateTime ?: return false

    val cancellationDeadline = departure - cancelUpToDays.days
    val now = Clock.System.now()

    return now < cancellationDeadline
}