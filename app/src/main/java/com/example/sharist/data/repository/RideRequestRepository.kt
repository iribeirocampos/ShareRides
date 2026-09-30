package com.example.sharist.data.repository

import android.content.Context
import android.util.Log
import com.example.sharist.data.local.dao.RideRequestDao
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


class RideRequestRepository(
    private val remote: StorageRemoteDataSource,
    private val dao: RideRequestDao
){
    suspend fun markSynced(rideRequest: RideRequest){
        dao.updateRideRequest(rideRequest.copy(syncState = SyncState.SYNCED.toString()))
    }

    suspend fun markDeleted(rideRequest: RideRequest){
        dao.updateRideRequest(rideRequest.copy(syncState = SyncState.PENDING_DELETE.toString()))
    }

    suspend fun addRideRequest(context: Context, rideRequest: RideRequest){
        // 1-Saving Ride locally
        dao.insertRideRequest(rideRequest)
        SessionManager.insertRideRequest(rideRequest)
        // 2- trying to sync with supabase
        val synced = try {
            remote.insertRideRequest(rideRequest)
        } catch (e: Exception) {
            null
        }
        if (synced != null) {
            markSynced(rideRequest)
            UiEventBus.send(UiEvent.Success("New Ride Request Added"))
        }else{
            Log.d("WORKER", "OFFLINE creation")
            UiEventBus.send(UiEvent.Warning("Unable to upload new ride request, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }
    suspend fun removeRideRequest(context:Context, rideRequest:RideRequest){
        markDeleted(rideRequest)
        SessionManager.removeRideRequest(rideRequest)
        val synced = try {
            remote.deleteRideRequest(rideRequest)
        } catch (e: Exception) {
            null
        }
        if (synced != null) {
            dao.deleteRideRequest(rideRequest)
            UiEventBus.send(UiEvent.Success("Ride Request Deleted"))
        }else{
            Log.d("WORKER", "OFFLINE deletion")
            UiEventBus.send(UiEvent.Warning("Unable to delete ride request, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }

    // Stops a recurring request from rolling forward (soft stop). Remote-first:
    // only flip locally once the server confirms, so the ValidationWorker (which
    // gates rolling on `active`) and the backend stay consistent.
    suspend fun stopRecurringRequest(context: Context, rideRequest: RideRequest){
        try {
            remote.setRideRequestActive(rideRequest.id, false)
            val updated = rideRequest.copy(active = false)
            dao.updateRideRequest(updated)
            SessionManager.updateRideRequest(updated)
            UiEventBus.send(UiEvent.Success("Recurrence stopped"))
        } catch (e: Exception) {
            Log.e("RideRequest", "Unable to stop recurrence", e)
            UiEventBus.send(UiEvent.Warning("Unable to reach server, please try again when online"))
        }
        SyncManager.enqueueSync(context)
    }

    suspend fun getUserRideRequests(userId: String): List <RideRequest>{
        val local = dao.getUserRideRequests(userId)
        if (!local.isEmpty()) return local
        Log.i("RidesRepository", "Rides not found locally, Looking in supabase")
        val remoteLocations = remote.getUserRideRequests(userId)
        remoteLocations.forEach { rideRequest ->
            dao.insertRideRequest(rideRequest)
        }
        return remoteLocations
    }

    suspend fun loadUserRideRequestsAsync(userId: String) {
        withContext(Dispatchers.IO) {
            val rideRequests = getUserRideRequests(userId)
            SessionManager.setMyRideRequests(rideRequests)
        }
    }

    suspend fun getRideRequest(rideRequestId:String):RideRequest?{
        val ride = CacheManager.get<RideRequest>(rideRequestId) ?: return remote.getRideRequest(rideRequestId)
        return ride
    }

    suspend fun loadAvailableRideRequests(context:Context, location: SimpleLocation):List<RideRequest> {
        try{
        val network = NetworkManager.getNetworkType(context)
        val cacheRange = LocationPolicy.cacheRadius(network)
        val fetchRange = LocationPolicy.fetchRadius(network)
        if (SessionManager.isWithinLoadedArea(location, cacheRange)||fetchRange<=0.0) {
            Log.d("LOCATION", "Location did not change enough, no need to fetch more")
            return CacheManager.getAll<RideRequest>()
        }
        Log.d("LOCATION", "Location did  change enough, fetching more rides")
        withContext(Dispatchers.IO) {
            val rideRequests = remote.getAvailableRideRequests(location, fetchRange)
            SessionManager.saveLoadedArea(location, fetchRange)
            rideRequests.forEach { rideRequest ->
                CacheManager.put<RideRequest>(rideRequest.id, rideRequest)
            }
            Log.d("LOADING", "Loaded {$rideRequests} RideRequests ")
        }
            ValidationManager.triggerImmediateValidation(context)
        return CacheManager.getAll<RideRequest>()
        }catch(e:Exception){
            UiEventBus.send(UiEvent.Error("Something went wrong, Unable to load Ride Requests"))
            return emptyList()
        }
    }
}
