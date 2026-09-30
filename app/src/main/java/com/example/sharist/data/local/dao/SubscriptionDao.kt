package com.example.sharist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.SubscribedRide
import com.example.sharist.data.model.Subscription
import kotlinx.coroutines.flow.Flow


@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscription WHERE id=:id")
    fun getSubscriptionById(id:String): Subscription

    @Query("SELECT * FROM subscription WHERE userRiderId = :id AND syncState!='PENDING_DELETE'")
    fun getUserRidesSubscribed(id:String): List<Subscription>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubscription(subscription: Subscription): Long

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)
    @Update
    suspend fun updateSubscription(subscription:Subscription)

    @Query("SELECT * FROM subscription WHERE syncState == 'PENDING_CREATE'")
    suspend fun getPendingSubscriptions(): List<Subscription>

    @Query("SELECT * FROM subscription WHERE syncState == 'PENDING_DELETE'")
    suspend fun getPendingSubscriptionsDelete(): List<Subscription>

    @Transaction
    @Query("""
    SELECT 
        ride.*,

        subscription.id AS subscription_id,
        subscription.rideId AS subscription_rideId,
        subscription.userRiderId AS subscription_userRiderId,
        subscription.onlyInRain AS subscription_onlyInRain,
        subscription.reoccurrence AS subscription_reoccurrence,
        subscription.syncState AS subscription_syncState

    FROM ride

    INNER JOIN subscription
        ON ride.id = subscription.rideId

    WHERE subscription.userRiderId = :userId
        AND subscription.syncState != 'PENDING_DELETE'
""")
    suspend fun getSubscribedRides(
        userId: String
    ): List<SubscribedRide>

    @Transaction
    @Query("""
    SELECT 
        ride.*,

        subscription.id AS subscription_id,
        subscription.rideId AS subscription_rideId,
        subscription.userRiderId AS subscription_userRiderId,
        subscription.onlyInRain AS subscription_onlyInRain,
        subscription.reoccurrence AS subscription_reoccurrence,
        subscription.syncState AS subscription_syncState

    FROM ride

    INNER JOIN subscription
        ON ride.id = subscription.rideId

    WHERE subscription.userRiderId = :userId
        AND subscription.syncState != 'PENDING_DELETE'
        AND subscription.onlyInRain = 1
""")
    suspend fun getSubscribedRidesWeather(
        userId: String
    ): List<SubscribedRide>


    @Query("""
    SELECT 
        ride.*,

        subscription.id AS subscription_id,
        subscription.userRiderId AS subscription_userRiderId,
        subscription.rideId AS subscription_rideId,
        subscription.onlyInRain AS subscription_onlyInRain,
        subscription.reoccurrence AS subscription_reoccurrence,
        subscription.syncState AS subscription_syncState

    FROM subscription
    INNER JOIN ride ON ride.id = subscription.rideId

    WHERE subscription.id = :subscriptionId
""")
    suspend fun getSubscribedRideBySubscriptionId(
        subscriptionId: String
    ): SubscribedRide

    @Query("SELECT * FROM ride_request WHERE syncState != 'PENDING_DELETE' AND driverId=:userId AND date > :now")
    suspend fun getAcceptedRequests(userId:String,now:String ):List<RideRequest>
}
