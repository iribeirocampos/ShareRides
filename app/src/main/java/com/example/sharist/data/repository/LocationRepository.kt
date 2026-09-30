package com.example.sharist.data.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.sharist.data.local.dao.LocationDao
import com.example.sharist.data.local.dao.UserDao
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.local.util.ImageCompressor
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.model.UserProfile
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.session.SessionManager
import com.example.sharist.sync.SyncManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.collections.filter

class LocationRepository(
    private val remote: StorageRemoteDataSource,
    private val local: FileStorageDataSource,
    private val dao: LocationDao
){

    suspend fun getLocations(userId: String): List <Location>{
        val local = dao.getUserLocations(userId)
        if (!local.isEmpty()) return local
        Log.i("UserRepository", "Locations not found locally, Looking in supabase")
        val remoteLocations = remote.getUserLocations(userId)
        remoteLocations.forEach { location ->
            dao.insertLocation(location)
        }
        return remoteLocations
    }

    suspend fun loadUserLocationsAsync(userId: String) {
        withContext(Dispatchers.IO) {
            val locations = getLocations(userId)
            SessionManager.setLocations(locations)
        }
    }

    suspend fun markDeleted(location:Location){
        dao.updateLocation(location.copy(syncState = SyncState.PENDING_DELETE.toString()))
    }

    suspend fun markSynced(location: Location) {
        dao.updateLocation(location.copy(syncState = SyncState.SYNCED.toString()))
    }

    suspend fun saveLocally(context: Context, location:Location){
       val updatedLocation =  if (location.remotePhotoUrl != null){
            val bytes = ImageCompressor.compressImage(
                context,
                location.remotePhotoUrl.toUri()
            )
            val localPath = local.saveImage(context, bytes, UUID.randomUUID().toString())
            location.copy(
                localPhotoUrl = localPath
            )
        }else{
            location
        }
        dao.insertLocation(updatedLocation)
        SessionManager.addLocation(updatedLocation)
    }

    suspend fun uploadLocation(context: Context, location:Location){
        val updatedLocation = if (location.remotePhotoUrl != null) {
            val bytes = ImageCompressor.compressImage(
                context,
                location.remotePhotoUrl.toUri()
            )
            val remoteUrl = remote.uploadPhoto("locations", bytes)

            location.copy(
                remotePhotoUrl = remoteUrl,
            )

        } else {
            location
        }
        remote.insertLocation(updatedLocation)
    }


    suspend fun add(context :Context, location:Location){
        // 1. always save locally first
        saveLocally(context,location)

        // 2. try remote sync
        val synced = try {
            uploadLocation(context, location)
        } catch (e: Exception) {
            null
        }
        // 3. if success → update local state
        if (synced != null) {
            markSynced(location)
            UiEventBus.send(UiEvent.Success("New Location Added Successfully"))
        }else{
            UiEventBus.send(UiEvent.Warning("Unable to upload new location, please confirm you have a connection"))
            Log.d("WORKER", "OFFLINE creation")
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }

    suspend fun remoteDeletion(location:Location){
        if (location.remotePhotoUrl!=null){
            val fileName = location.remotePhotoUrl.substringAfterLast("/")
            remote.deletePhoto("locations", fileName)
        }
        remote.deleteLocation(location)
    }

    suspend fun remove(context : Context, location:Location){
        // 1 - Marking for deletion locally
        markDeleted(location)
        if (location.localPhotoUrl!=null){
            local.deleteImage(context, location.localPhotoUrl)
        }
        SessionManager.removeLocation(location)
        // 2. try remote sync
        val synced = try {
            remoteDeletion( location)
        } catch (e: Exception) {
            null
        }
        if (synced != null) {
            dao.deleteLocation(location) // only deletes after sync successful
            UiEventBus.send(UiEvent.Success("Location Deleted Successfully"))
        }else{
            Log.d("WORKER", "OFFLINE deletion")
            UiEventBus.send(UiEvent.Warning("Unable to delete location, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context)
    }

    fun get(locationId: String): Location?{
        return SessionManager.getLocation(locationId)
    }

    fun getLocations(): StateFlow<List<Location>> {
        return SessionManager.myLocations
    }


}

