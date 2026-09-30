package com.example.sharist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class UserType {
    DRIVER,
    RIDER;
    fun displayName(): String {
        return when (this) {
            DRIVER -> "Driver"
            RIDER -> "Rider"
        }
    }
}


@Serializable
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String,
    val type: UserType,
    val username: String,
    val photoUrl:String?=null,
    val localPhotoPath: String? = null,
    val averageRating: Float = 0f,
    val syncState: String = SyncState.SYNCED.toString()
    ){

}