package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sharist.data.model.RideRequest
import kotlinx.coroutines.flow.Flow


@Dao
interface RideRequestDao {
    @Query("SELECT * FROM ride_request")
    fun getAllRideRequests(): Flow<List<RideRequest>>

    @Query("SELECT * FROM ride_request WHERE userId = :id AND syncState!='PENDING_DELETE'")
    fun getUserRideRequests(id:String): List<RideRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRideRequest(rideRequest: RideRequest)

    @Delete
    suspend fun deleteRideRequest(rideRequest: RideRequest)
    @Update
    suspend fun updateRideRequest(rideRequests:RideRequest)

    @Query("SELECT * FROM ride_request WHERE syncState == 'PENDING_CREATE'")
    suspend fun getPendingRideRequests(): List<RideRequest>

    @Query("SELECT * FROM ride_request WHERE syncState == 'PENDING_DELETE'")
    suspend fun getPendingRideRequestsDelete(): List<RideRequest>

    @Query("SELECT * FROM ride_request WHERE syncState=='PENDING_CREATE' AND driverId=:userId")
    suspend fun getPendingRideRequestsSubscriptions(userId:String):List<RideRequest>

    @Query("SELECT * FROM ride_request WHERE syncState == 'PENDING_DELETE' AND driverId=:userId")
    suspend fun getPendingRideRequestsSubscriptionsDelete(userId: String): List<RideRequest>

    // Recurring requests owned by this rider that are still active and whose
    // start time has passed, due to be rolled forward to their next occurrence.
    @Query("SELECT * FROM ride_request WHERE userId = :userId AND syncState = 'SYNCED' AND active = 1 AND (isWeekly = 1 OR isDaily = 1) AND date < :now")
    suspend fun getOwnRecurringRequestsDue(userId: String, now: String): List<RideRequest>


}
