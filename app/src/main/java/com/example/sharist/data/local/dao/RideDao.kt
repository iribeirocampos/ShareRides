package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.Ride
import kotlinx.coroutines.flow.Flow


@Dao
interface RideDao {
    @Query("SELECT * FROM ride")
    fun getAllRides(): Flow<List<Ride>>
    @Query("SELECT * FROM ride WHERE id=:id")
    fun getRideById(id:String): Ride

    @Query("SELECT * FROM ride WHERE userId = :id AND syncState!='PENDING_DELETE'")
    fun getUserRides(id:String): List<Ride>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: Ride)

    @Delete
    suspend fun deleteRide(ride: Ride)
    @Update
    suspend fun updateRide(ride:Ride)

    @Query("SELECT * FROM ride WHERE syncState == 'PENDING_CREATE'")
    suspend fun getPendingRides(): List<Ride>

    @Query("SELECT * FROM ride WHERE syncState == 'PENDING_DELETE'")
    suspend fun getPendingRidesDelete(): List<Ride>

    // Recurring rides owned by this driver whose start time has passed and are
    // due to be rolled forward to their next occurrence.
    @Query("SELECT * FROM ride WHERE userId = :userId AND syncState = 'SYNCED' AND (isWeekly = 1 OR isDaily = 1) AND departureDateTime IS NOT NULL AND departureDateTime < :now")
    suspend fun getOwnRecurringRidesDue(userId: String, now: String): List<Ride>

}
