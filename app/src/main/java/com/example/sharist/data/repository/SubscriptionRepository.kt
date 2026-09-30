package com.example.sharist.data.repository

import android.content.Context
import android.util.Log
import com.example.sharist.data.local.dao.RideDao
import com.example.sharist.data.local.dao.RideRequestDao
import com.example.sharist.data.local.dao.SubscriptionDao
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.SubscribedRide
import com.example.sharist.data.model.Subscription
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.session.CacheManager
import com.example.sharist.data.session.SessionManager
import com.example.sharist.sync.SyncManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.time.Clock

class SubscriptionRepository(
    private val remote: StorageRemoteDataSource,
    private val dao: SubscriptionDao,
    private val rideDao: RideDao,
    private val rideRequestDao: RideRequestDao
){

    suspend fun getSubscribedRides(userId: String): List<SubscribedRide> {
        val local = dao.getSubscribedRides(userId)
        if (!local.isEmpty()) return local
        Log.i("SubscriptionRepository", "Subscriptions not found locally, Looking in supabase")
        return try {
            val subscriptions = remote.getUserSubscriptions(userId)
            val rideIds = subscriptions.map { it.rideId }.distinct()
            val rides = remote.getSubscribedRides(rideIds)
            // IMPORTANT: build lookup map for fast join
            val rideMap = rides.associateBy { it.id }
            val remoteRides = subscriptions.mapNotNull { sub ->
                val ride = rideMap[sub.rideId] ?: return@mapNotNull null
                SubscribedRide(ride = ride, subscription = sub)
            }
            remoteRides.forEach { subscribedRide ->
                dao.insertSubscription(subscribedRide.subscription)
                rideDao.insertRide(subscribedRide.ride)
            }
            remoteRides
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.w("SubscriptionRepository", "Unable to reach server", e)
            emptyList()
        }
    }

    suspend fun loadMyRideSubscriptions(userId:String){
        withContext(Dispatchers.IO) {
            val subscribedRides = getSubscribedRides(userId)
            SessionManager.setMyRideSubscriptions(subscribedRides)
        }
    }

    suspend fun markSynced(subscription: Subscription){
        dao.updateSubscription(subscription.copy(syncState = SyncState.SYNCED.toString()))
    }

    suspend fun markSynced(rideRequest: RideRequest){
        rideRequestDao.updateRideRequest(rideRequest.copy(syncState = SyncState.SYNCED.toString()))
    }
    suspend fun  markDeleted(rideRequest: RideRequest) {
        rideRequestDao.updateRideRequest(rideRequest.copy(syncState = SyncState.PENDING_DELETE.toString()))
    }


    suspend fun markDeleted(subscription: Subscription){
        dao.updateSubscription(subscription.copy(syncState = SyncState.PENDING_DELETE.toString()))
    }

    suspend fun abortSubscription(subscription:Subscription){
        dao.deleteSubscription(subscription)
        SessionManager.removeRideSubscription(subscription)
    }

    suspend fun remoteSubscribe(subscription:Subscription):String{
        Log.d("SUBSCRIBE", "REMOTE SUBSCRIBE")
        val result = remote.insertSubscription(subscription)
        Log.d("SUBSCRIBE", "REMOTE SUBSCRIBE result $result")
        when (result) {
            "OK" -> {
                markSynced(subscription)
                UiEventBus.send(UiEvent.Success("Subscription Successful"))
            }
            "NO_SEATS" -> {
                abortSubscription(subscription)
                UiEventBus.send(UiEvent.Error("No available seats"))
            }
            "RIDE_NOT_FOUND" -> {
                abortSubscription(subscription)
                UiEventBus.send(UiEvent.Error("Ride no longer Exists"))
            }
            "ALREADY_SUBSCRIBED" -> {
                markSynced(subscription)
                UiEventBus.send(UiEvent.Warning("You are already subscribed"))
            }
            else -> {
                abortSubscription(subscription)
                UiEventBus.send(UiEvent.Error("Unable to Subscribe: $result"))
            }
        }
        return result
    }
    suspend fun subscribeRide(context: Context, rideId:String, riderUserId:String, onlyInRain: Boolean){
        val ride = CacheManager.get<Ride>(rideId)
        if (ride==null){
            UiEventBus.send(UiEvent.Error("Something went wrong, can't find the Ride"))
            return
        }
        val subscription = Subscription(
            id = UUID.randomUUID().toString(),
            rideId = rideId,
            userRiderId = riderUserId,
            onlyInRain = onlyInRain,
            // A subscription to a recurring ride is itself recurring: it carries
            // over automatically as the ride's date is rolled forward.
            reoccurrence = ride.isWeekly || ride.isDaily
        )
        val insertResult = dao.insertSubscription(subscription)
        // 🔥 DUPLICATE
        if (insertResult == -1L) {
            UiEventBus.send(UiEvent.Warning("You are already subscribed"))
            return
        }
        Log.d("SUBSCRIBE", "Got Ride $subscription")
        val subscribedRide = SubscribedRide(
            subscription=subscription,
            ride= ride
        )
        SessionManager.addRideSubscription(subscribedRide)
        CacheManager.remove<Ride>(ride.id)
        val synced = try {
            remoteSubscribe(subscription)
        } catch (e: Exception) {
            null
        }
        if(synced==null){
            UiEventBus.send(UiEvent.Warning("Unable to connect to Server, please connect to sync"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }


    suspend fun remoteUnsubscribe(subscription: Subscription):String{
        val result = remote.deleteSubscription(subscription)
        when(result){
            "OK" -> {
                dao.deleteSubscription(subscription)
                UiEventBus.send(UiEvent.Success("Unsubscribed Successfully"))
                SessionManager.removeRideSubscription(subscription)
            }
            "TOO_LATE" -> {
                markSynced(subscription)
                UiEventBus.send(UiEvent.Warning("Cannot unsubscribe, its too late"))
            }
            "NOT_FOUND" -> {
                dao.deleteSubscription(subscription)
                UiEventBus.send(UiEvent.Error("Subscription Not Found, deleting locally"))
            }
        }
        return result
    }
    suspend fun unsubscribeRide(subscriptionRide: SubscribedRide){
        markDeleted(subscriptionRide.subscription)
        val result = try{
            remoteUnsubscribe(subscriptionRide.subscription)
        }catch (e: Exception){
            Log.d("DELETE", e.toString())
            null
        }
        if(result==null){
            UiEventBus.send(UiEvent.Warning("Unable to connect to Server, please connect to sync"))
        }

    }
    suspend fun unsubscribeRide(subscription: Subscription){
        markDeleted(subscription)
        val result = try{
            remoteUnsubscribe(subscription)
        }catch (e: Exception){
            Log.d("DELETE", e.toString())
            null
        }
        if(result==null){
            UiEventBus.send(UiEvent.Warning("Unable to connect to Server, please connect to sync"))
        }

    }
    suspend fun unsubscribeWeatherRide(subscriptionRide: SubscribedRide){
        unsubscribeRide(subscriptionRide)
        UiEventBus.send(UiEvent.Warning("Unsubscribed your ride at ${subscriptionRide.ride.departureDateTime}, It's not raining!"))
    }

    suspend fun remoteRideRequestAccept(rideRequest: RideRequest):String{
        val result =  remote.acceptRideRequest(rideRequest.id, rideRequest.driverId!!)
        when(result){
            "OK" -> {
                markSynced(rideRequest)
                UiEventBus.send(UiEvent.Success("Ride Accepted Successfully"))
            }
            "ALREADY_HAS_DRIVER" -> {
                rideRequestDao.deleteRideRequest(rideRequest)
                UiEventBus.send(UiEvent.Warning("Cannot unsubscribe, Already has driver"))
            }
            "NOT_FOUND" -> {
                rideRequestDao.deleteRideRequest(rideRequest)
                UiEventBus.send(UiEvent.Error("Subscription Not Found"))
            }
        }
        return result
    }

    suspend fun acceptRideRequest(context: Context, rideRequest: RideRequest, driverId:String){
        if (rideRequest.driverId!=null){
            UiEventBus.send(UiEvent.Error("This Ride is no longer available"))
            return
        }
        val updated = rideRequest.copy(driverId = driverId,syncState= SyncState.PENDING_CREATE.toString())
        rideRequestDao.insertRideRequest(updated)
        val result = try{
            remoteRideRequestAccept(updated)
        }catch (e:Exception){
            Log.d("SESSION", "Exception: ${e.toString()}")
            null
        }
        SessionManager.addRideRequestAccepted(rideRequest.id)
        if(result==null){
            UiEventBus.send(UiEvent.Warning("Unable to connect to Server, please connect to sync"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }


    suspend fun cancelRideRequest(context: Context, rideRequest: RideRequest){
        markDeleted(rideRequest)
        SessionManager.removeAcceptedRideRequest(rideRequest)
        try{
            remote.cancelRideRequest(rideRequest.id)
            rideRequestDao.deleteRideRequest(rideRequest)
            UiEventBus.send(UiEvent.Success("Ride Request Cancelled Successfully"))
        }catch(e:Exception){
            UiEventBus.send(UiEvent.Warning("Unable to connect to Server, please connect to sync"))
            Log.d("RideRequest", "Unable to Cancel in server")
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }

    suspend fun checkRideStillValid(context:Context, subscription: Subscription){
        val remoteRide = remote.getRide(subscription.rideId)
        Log.d("VALIDATION", "Request ${remoteRide.toString()}")
        if (remoteRide==null){
            UiEventBus.send(UiEvent.Warning("Your Ride ${subscription.id} was Cancelled by driver"))
            unsubscribeRide(subscription)
            return
        }
        // Ride still exists: pull through any remote changes (e.g. a recurring
        // ride the driver rolled forward) so the rider sees the updated date.
        val refreshed = remoteRide.copy(syncState = SyncState.SYNCED.toString())
        rideDao.insertRide(refreshed) // REPLACE on conflict acts as an upsert
        SessionManager.updateSubscribedRide(refreshed)
    }

    suspend fun checkRequestStillValid(context:Context, rideRequest:RideRequest){
        val remoteRequest = remote.getRideRequest(rideRequest.id)
        Log.d("VALIDATION", "Request ${remoteRequest.toString()}")
        if (remoteRequest==null){
            UiEventBus.send(UiEvent.Warning("Your Accepted Ride Request ${rideRequest.id} is no longer valid, Cancelled by rider"))
            cancelRideRequest(context, rideRequest)
            return
        }
        // Request still exists: pull through any remote changes (e.g. a recurring
        // request the rider rolled forward) so the driver sees the updated date.
        val refreshed = remoteRequest.copy(syncState = SyncState.SYNCED.toString())
        rideRequestDao.insertRideRequest(refreshed) // REPLACE on conflict acts as an upsert
        SessionManager.updateAcceptedRideRequest(refreshed)
    }

    suspend fun loadMyAcceptedRideRequests(userId:String){
        val now = Clock.System.now().toString()
        val acceptedRequests = dao.getAcceptedRequests(userId, now)
        SessionManager.setAcceptedRideRequests(acceptedRequests)
        Log.d("LOADING", "Loaded My Requests: $acceptedRequests")
    }
}
