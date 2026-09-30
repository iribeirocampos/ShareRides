package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
@Serializable
data class SimpleLocation(
    @PrimaryKey val id: String,
    val address: String,
    val latitude:Double,
    val longitude: Double,
)