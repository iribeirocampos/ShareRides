package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "vehicle")
data class Vehicle(
    @PrimaryKey val id: String,
    val brand: String,
    val model: String,
    val userId: String,
    val licencePlate:String,
    val color:String,
    val capacity: Int,
    val remotePhotoUrl:String?= null,
    val localPhotoUrl:String? = null,
    val syncState: String = SyncState.PENDING_CREATE.toString()
) {
}