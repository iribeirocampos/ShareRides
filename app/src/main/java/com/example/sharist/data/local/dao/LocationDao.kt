package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sharist.data.model.Location
import kotlinx.coroutines.flow.Flow


@Dao
interface LocationDao {
    @Query("SELECT * FROM location")
    fun getAllLocations(): Flow<List<Location>>

    @Query("SELECT * FROM location WHERE userId = :id AND syncState!='PENDING_DELETE'")
    fun getUserLocations(id:String): List<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)

    @Query("UPDATE location SET remotePhotoUrl = :remoteUrl, localPhotoUrl=:localPath WHERE id = :id")
    suspend fun updatePhotoUrl(id:String, remoteUrl:String, localPath:String)

    @Update
    suspend fun updateLocation(location:Location)

    @Query("SELECT * FROM location WHERE syncState == 'PENDING_CREATE'")
    suspend fun getPendingLocations(): List<Location>

    @Query("SELECT * FROM location WHERE syncState == 'PENDING_DELETE'")
    suspend fun getPendingLocationsDelete(): List<Location>
}
