package com.example.sharist.data.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.sharist.data.local.dao.LocationDao
import com.example.sharist.data.local.dao.VehicleDao
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.local.util.ImageCompressor
import com.example.sharist.data.model.Location
import com.example.sharist.data.model.SyncState
import com.example.sharist.data.model.Vehicle
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.session.SessionManager
import com.example.sharist.sync.SyncManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID


class VehicleRepository(
    private val remote: StorageRemoteDataSource,
    private val local: FileStorageDataSource,
    private val dao: VehicleDao)
{


    suspend fun markSynced(vehicle: Vehicle) {
        dao.updateVehicle(vehicle.copy(syncState = "SYNCED"))
    }


    suspend fun saveLocally(context: Context, vehicle:Vehicle){
        val updatedVehicle =  if (vehicle.remotePhotoUrl != null){
            val bytes = ImageCompressor.compressImage(
                context,
                vehicle.remotePhotoUrl.toUri()
            )
            val localPath = local.saveImage(context, bytes, UUID.randomUUID().toString())
            vehicle.copy(
                localPhotoUrl = localPath
            )
        }else{
            vehicle
        }
        dao.insertVehicle(updatedVehicle)
        SessionManager.addVehicle(updatedVehicle)
    }

    suspend fun uploadVehicle(context: Context, vehicle:Vehicle){
        val updatedVehicle = if (vehicle.remotePhotoUrl != null) {
            val bytes = ImageCompressor.compressImage(
                context,
                vehicle.remotePhotoUrl.toUri()
            )
            val remoteUrl = remote.uploadPhoto("vehicle", bytes)

            vehicle.copy(
                remotePhotoUrl = remoteUrl,
            )

        } else {
            vehicle
        }
        remote.insertVehicle(updatedVehicle)
    }
    suspend fun add(context: Context, vehicle: Vehicle) {
        // 1. always save locally first
        saveLocally(context,vehicle)

        // 2. try remote sync
        val synced = try {
            uploadVehicle(context, vehicle)
        } catch (e: Exception) {
            null
        }
        // 3. if success → update local state
        if (synced != null) {
            markSynced(vehicle)
            UiEventBus.send(UiEvent.Success("New Vehicle Added"))
        }else{
            UiEventBus.send(UiEvent.Warning("Unable to upload new vehicle, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context) // Tries to Sync eventual objects that are not synced
    }

    suspend fun deleteCurrentPhoto(context:Context, vehicle: Vehicle){
        if (vehicle.remotePhotoUrl!=null){
            val fileName = vehicle.remotePhotoUrl.substringAfterLast("/")
            remote.deletePhoto("vehicle", fileName)
        }
    }

    suspend fun markDeleted(vehicle:Vehicle){
        dao.updateVehicle(vehicle.copy(syncState = SyncState.PENDING_DELETE.toString()))
    }

    suspend fun remoteDeletion(vehicle:Vehicle){
        if (vehicle.remotePhotoUrl!=null){
            val fileName = vehicle.remotePhotoUrl.substringAfterLast("/")
            remote.deletePhoto("vehicle", fileName)
        }
        remote.removeVehicle(vehicle)
    }
    suspend fun remove(context:Context, vehicle:Vehicle){
        markDeleted(vehicle)
        if (vehicle.localPhotoUrl!=null){
            local.deleteImage(context, vehicle.localPhotoUrl)
        }
        SessionManager.removeVehicle(vehicle)
        val synced = try {
            remoteDeletion( vehicle)
        } catch (e: Exception) {
            null
        }
        if (synced != null) {
            dao.deleteVehicle(vehicle) // only deletes after sync successful
            UiEventBus.send(UiEvent.Success("Vehicle Deleted"))
        }else{
            Log.d("WORKER", "OFFLINE deletion")
            UiEventBus.send(UiEvent.Warning("Unable to delete vehicle, please confirm you have a connection"))
        }
        SyncManager.enqueueSync(context)
    }

    suspend fun getVehicles(userId: String): List <Vehicle>{
        val local = dao.getUserVehicles(userId)
        if (!local.isEmpty()) return local
        Log.i("UserRepository", "Vehicles not found locally, Looking in supabase")
        // TODO: Should work eve if theres no connection - Maybe set a try/catch
        val remoteLocations = remote.getUserVehicles(userId)
        remoteLocations.forEach { vehicle ->
            dao.insertVehicle(vehicle)
        }
        return remoteLocations
    }

    suspend fun loadMyVehiclesAsync(userId:String) {
        withContext(Dispatchers.IO) {
            SessionManager.setMyVehicles(getVehicles(userId))
        }
    }



}
