package com.example.sharist.data.repository

import android.content.Context
import android.util.Log
import com.example.sharist.data.local.dao.RideDao
import com.example.sharist.data.local.util.NetworkManager
import com.example.sharist.data.model.Ride
import com.example.sharist.data.model.RideRequest
import com.example.sharist.data.model.SimpleLocation
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.policies.LocationPolicy
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.session.CacheManager
import com.example.sharist.data.session.SessionManager
import com.example.sharist.sync.SyncManager
import com.example.sharist.sync.ValidationManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RideRepository(
    private val remote: StorageRemoteDataSource,
    private val dao: RideDao,
){

    suspend fun markSynced(ride: Ride){
        dao.updateRide(ride.copy(syncState = SyncState.SYNCED.toString()))
    }

    suspend fun markDeleted(ride: Ride){
        dao.updateRide(ride.copy(syncState = SyncState.PENDING_DELETE.toString()))
    }


    suspend fun addRide(context: Context, ride: Ride){
        // 1-Saving Ride locally
        dao.insertRide(ride)
        SessionManager.addRide(ride)
        // 2- trying to sync with supabase
        val synced = try {
            remote.insertRide(ride)
        } catch (e: Exception) {
            null
        }
        if (synced != null) {
            markSynced(ride)
            UiEventBus.send(UiEvent.Success("New Ride Added"))
        }else{
            Log.d("WORKER", "OFFLINE creation")
            UiEventBus.send(UiEvent.Warning("Unable to upload new ride, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }


    suspend fun removeRide(context:Context, ride:Ride){
        markDeleted(ride)
        SessionManager.removeRide(ride)
        val synced = try {
            remote.deleteRide(ride)
        } catch (e: Exception) {
            null
        }
        if (synced != null) {
            dao.deleteRide(ride)
            UiEventBus.send(UiEvent.Success("Ride Deleted Successfully"))
        }else{
            UiEventBus.send(UiEvent.Warning("Unable to delete the ride in server, please confirm you have a connection"))
            Log.d("WORKER", "OFFLINE deletion")
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }
    suspend fun getUserRides(userId: String): List<Ride> {
        val local = dao.getUserRides(userId)
        if (!local.isEmpty()) return local
        Log.i("RidesRepository", "Rides not found locally, Looking in supabase")
        return try {
            val remoteRides = remote.getUserRides(userId)
            remoteRides.forEach { ride -> dao.insertRide(ride) }
            remoteRides
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.w("RidesRepository", "Unable to reach server", e)
            emptyList()
        }
    }

    suspend fun loadUserRidesAsync(userId: String) {
        withContext(Dispatchers.IO) {
            val rides = getUserRides(userId)
            Log.d("RIDES", "Loaded $rides")
            SessionManager.setMyRides(rides)
        }
    }

    suspend fun getRide(rideId:String):Ride?{
        val ride = CacheManager.get<Ride>(rideId) ?: return remote.getRide(rideId)
        return ride
    }

    suspend fun stopRecurringRide(ride: Ride){
        try {
            remote.removeRideRecurrence(ride.id)
            val updated = ride.copy(isWeekly = false, isDaily = false)
            dao.updateRide(updated)
            SessionManager.updateMyRides(updated)
            UiEventBus.send(UiEvent.Success("Recurrence stopped"))
        } catch (e: Exception) {
            Log.e("RideRequest", "Unable to stop recurrence", e)
            UiEventBus.send(UiEvent.Warning("Unable to reach server, please try again when online"))
        }

    }

    suspend fun loadAvailableRides(context:Context, location: SimpleLocation): List<Ride> {
        try {
            val network = NetworkManager.getNetworkType(context)
            val cacheRange = LocationPolicy.cacheRadius(network)
            val fetchRange = LocationPolicy.fetchRadius(network)

            if (SessionManager.isWithinLoadedArea(location, cacheRange)||fetchRange<=0.0) {
                Log.d("LOCATION", "Location did not change enough, no need to fetch more")
                return CacheManager.getAll<Ride>()
            }
            Log.d("LOCATION", "Location did change enough, fetching more rides")
            try {
                withContext(Dispatchers.IO) {
                    val rides = remote.getAvailableRides(location, fetchRange)
                    SessionManager.saveLoadedArea(location, fetchRange)
                    rides.forEach { ride -> CacheManager.put<Ride>(ride.id, ride) }
                    Log.d("LOCATION", "Loaded ${rides.size} rides")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w("RidesRepository", "Unable to reach server", e)
            }
            ValidationManager.triggerImmediateValidation(context)
            return CacheManager.getAll<Ride>()
        }catch (e: Exception){
            UiEventBus.send(UiEvent.Error("Something went wrong, Unable to load Rides"))
            return emptyList()
        }
        }
}