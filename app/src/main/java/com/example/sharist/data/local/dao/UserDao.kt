package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.sharist.data.model.UserProfile
import androidx.room.Update
@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile")
    fun getAllUsers(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profile WHERE id = :id")
    fun getUser(id:String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Delete
    suspend fun deleteUser(user: UserProfile)

    @Query("UPDATE user_profile SET photoUrl = :remoteUrl, localPhotoPath=:localPath WHERE id = :id")
    suspend fun updatePhotoUrl(id:String, remoteUrl:String, localPath:String)

    @Update
    suspend fun update(user:UserProfile)

    @Query("SELECT * FROM user_profile WHERE syncState == 'PENDING_CREATE'")
    suspend fun getPendingUser(): List<UserProfile>
}
