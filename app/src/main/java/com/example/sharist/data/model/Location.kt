package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "location")
data class Location(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude:Double,
    val longitude: Double,
    val userId: String,
    val remotePhotoUrl: String? = null,
    val localPhotoUrl: String? = null,
    val syncState: String = SyncState.PENDING_CREATE.toString()
)