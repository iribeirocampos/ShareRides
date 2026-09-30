package com.example.sharist.data.model

import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Serializable
@Entity(tableName="auth_session")
data class AuthSessionEntity(
    // One Row Table
    @PrimaryKey val id:Int=1,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
    val userId: String
)