package com.example.sharist.sync

import android.content.Context
import android.util.Log
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.remote.StorageRemoteDataSource
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sharist.data.local.datasource.FileStorageDataSource
import com.example.sharist.data.repository.LocationRepository
import com.example.sharist.data.repository.ReviewRepository
import com.example.sharist.data.repository.RideRepository
import com.example.sharist.data.repository.RideRequestRepository
import com.example.sharist.data.repository.SubscriptionRepository
import com.example.sharist.data.repository.UserRepository
import com.example.sharist.data.repository.VehicleRepository
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            var failed = false

            runCatching { syncLocations() }.onFailure { failed = true }
            runCatching { syncVehicles() }.onFailure { failed = true }
            runCatching { syncUserPhoto() }.onFailure { failed = true }
            runCatching { syncRide() }.onFailure { failed = true }
            runCatching { syncRideRequest() }.onFailure { failed = true }
            runCatching { syncSubscriptions() }.onFailure { failed = true }
            runCatching { syncRideRequestSubscriptions() }.onFailure { failed = true }
            runCatching { syncReviews() }.onFailure { failed = true }

            return if (failed) Result.retry() else Result.success()
        } catch (e: Exception) {
            Log.e("Worker", "Error: $e")
            Result.retry()
        }
    }
    private suspend fun syncLocations() :Result{
        val dao = SharistDatabase.getDatabase(applicationContext).locationDao()
        val remote = StorageRemoteDataSource()
        val local = FileStorageDataSource()
        val repository = LocationRepository(remote, local, dao)

        val pending = dao.getPendingLocations()
        val pendingDelete = dao.getPendingLocationsDelete()
        var failed: Boolean=false

        Log.d("WORKER","Found ${pending.size} Locations to sync")
        Log.d("WORKER","Found ${pendingDelete.size} Locations to Delete")

        pending.forEach { location ->
            try {
                repository.uploadLocation(applicationContext, location)
                repository.markSynced(location)
                UiEventBus.send(UiEvent.Success("New Location Added to the Server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${location.id}", e)
                failed = true
            }
        }
        pendingDelete.forEach { location ->
            try {
                repository.remoteDeletion(location)
                dao.deleteLocation(location)
                UiEventBus.send(UiEvent.Success("Location Deleted from server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${location.id}", e)
                failed = true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()
    }
    private suspend fun syncVehicles(): Result{
        val dao = SharistDatabase.getDatabase(applicationContext).vehicleDao()
        val remote = StorageRemoteDataSource()
        val local = FileStorageDataSource()
        val repository = VehicleRepository(remote, local, dao)

        val pending = dao.getPendingVehicles()
        val pendingDeletions = dao.getPendingVehicleDelete()
        Log.d("WORKER","Found ${pending.size} Vehicles to sync")
        Log.d("WORKER","Found ${pendingDeletions.size} Vehicles to Delete")
        var failed: Boolean=false

        pending.forEach { vehicle ->
            try {
                repository.uploadVehicle(applicationContext, vehicle)
                repository.markSynced(vehicle)
                UiEventBus.send(UiEvent.Success("New Vehicle Added to the server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${vehicle.id}", e)
                failed = true
            }
        }
        pendingDeletions.forEach { vehicle ->
            try {
                repository.remoteDeletion(vehicle)
                dao.deleteVehicle(vehicle)
                UiEventBus.send(UiEvent.Success("Vehicle Deleted from server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${vehicle.id}", e)
                failed= true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()
    }
    private suspend fun syncUserPhoto():Result{
        val db = SharistDatabase.getDatabase(applicationContext)
        val dao = db.userDao()
        val authDao = db.authDao()
        val remote = StorageRemoteDataSource()
        val local = FileStorageDataSource()
        val repository = UserRepository(remote, local, dao, authDao)

        val pending = dao.getPendingUser()
        var failed= false

        Log.d("WORKER","Found ${pending.size} Users to sync")

        pending.forEach { user ->
            try {
                repository.updateRemote(applicationContext, user)
                UiEventBus.send(UiEvent.Success("New User photo uploaded to server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${user.id}", e)
                failed=true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()
    }
    private suspend fun syncRide():Result{
        val dao = SharistDatabase.getDatabase(applicationContext).rideDao()
        val remote = StorageRemoteDataSource()
        val repository = RideRepository(remote, dao)

        val pending = dao.getPendingRides()
        val pendingDeletions = dao.getPendingRidesDelete()
        var failed= false

        Log.d("WORKER","Found ${pending.size} Rides to sync")
        pending.forEach { ride ->
            try {
                remote.insertRide(ride)
                repository.markSynced(ride)
                UiEventBus.send(UiEvent.Success("New Ride Added to the server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${ride.id}", e)
                failed=true
            }
        }
        pendingDeletions.forEach { ride ->
            try {
                remote.deleteRide(ride)
                dao.deleteRide(ride)
                UiEventBus.send(UiEvent.Success("Ride Deleted from server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${ride.id}", e)
                failed= true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()
    }
    private suspend fun syncRideRequest():Result{
        val dao = SharistDatabase.getDatabase(applicationContext).rideRequestDao()
        val remote = StorageRemoteDataSource()
        val repository = RideRequestRepository(remote, dao)

        val pending = dao.getPendingRideRequests()
        val pendingDeletions = dao.getPendingRideRequestsDelete()
        var failed= false

        Log.d("WORKER","Found ${pending.size} RideRequests to sync")
        pending.forEach { rideRequest ->
            try {
                remote.insertRideRequest(rideRequest)
                repository.markSynced(rideRequest)
                UiEventBus.send(UiEvent.Success("New Ride Request Added"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${rideRequest.id}", e)
                failed=true
            }
        }
        pendingDeletions.forEach { rideRequest ->
            try {
                remote.deleteRideRequest(rideRequest)
                dao.deleteRideRequest(rideRequest)
                UiEventBus.send(UiEvent.Success("Ride Request Deleted from server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${rideRequest.id}", e)
                failed= true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()
    }

    private suspend fun syncSubscriptions():Result{
        val db = SharistDatabase.getDatabase(applicationContext)
        val dao = db.subscriptionDao()
        val rideDao = db.rideDao()
        val rideRequestDao = db.rideRequestDao()
        val remote = StorageRemoteDataSource()
        val repository = SubscriptionRepository(remote, dao, rideDao, rideRequestDao)
        var failed= false

        val pending = dao.getPendingSubscriptions()
        val pendingDeletions = dao.getPendingSubscriptionsDelete()
        pending.forEach { subscription ->
            try {
                if (repository.remoteSubscribe(subscription) !="OK"){
                    failed =true
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${subscription.id}", e)
                failed=true
            }
        }
        pendingDeletions.forEach { subscription ->
            try {
                if (repository.remoteUnsubscribe(subscription)!="OK"){
                    failed=true
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${subscription.id}", e)
                failed= true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()

    }
    private suspend fun syncRideRequestSubscriptions():Result{
        val db = SharistDatabase.getDatabase(applicationContext)
        val dao = db.subscriptionDao()
        val rideDao = db.rideDao()
        val rideRequestDao = db.rideRequestDao()
        val remote = StorageRemoteDataSource()
        val repository = SubscriptionRepository(remote, dao, rideDao, rideRequestDao)
        var failed= false
        val userId = SessionManager.user.value?.id ?: return Result.retry()
        val pending = rideRequestDao.getPendingRideRequestsSubscriptions(userId)
        val pendingDeletions = rideRequestDao.getPendingRideRequestsSubscriptionsDelete(userId)
        pending.forEach { rideRequest ->
            try {
                if (repository.remoteRideRequestAccept(rideRequest) !="OK"){
                    failed =true
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${rideRequest.id}", e)
                failed=true
            }
        }
        pendingDeletions.forEach { rideRequest ->
            try {
                remote.cancelRideRequest(rideRequest.id)
                rideRequestDao.deleteRideRequest(rideRequest)
                UiEventBus.send(UiEvent.Success("Ride Request Cancelled Successfully"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${rideRequest.id}", e)
                failed= true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()

    }

    private suspend fun syncReviews():Result{
        val dao = SharistDatabase.getDatabase(applicationContext).reviewDao()
        val remote = StorageRemoteDataSource()
        val repository = ReviewRepository(remote, dao)

        val pending = dao.getPendingReviews()
        var failed: Boolean=false

        Log.d("WORKER","Found ${pending.size} Reviews to sync")

        pending.forEach { review ->
            try {
                remote.insertReview(review)
                repository.markSynced(review)
                UiEventBus.send(UiEvent.Success("New Review Added to the Server"))
            } catch (e: Exception) {
                Log.e("SYNC", "Failed syncing item ${review.id}", e)
                failed = true
            }
        }
        if (failed){
            return Result.retry()
        }
        return Result.success()
    }
}
