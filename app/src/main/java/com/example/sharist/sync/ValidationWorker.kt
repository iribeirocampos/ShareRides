package com.example.sharist.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sharist.data.local.dao.RideDao
import com.example.sharist.data.local.dao.RideRequestDao
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.model.UserType
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.SubscriptionRepository
import com.example.sharist.data.session.SessionManager
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.days


class ValidationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("VALIDATION", "Running Validation")
            val db = SharistDatabase.getDatabase(applicationContext)
            val dao = db.subscriptionDao()
            val now = Clock.System.now()
            val nowString = now.toString()
            val rideDao = db.rideDao()
            val rideRequestDao = db.rideRequestDao()
            val remote = StorageRemoteDataSource()
            val repository = SubscriptionRepository(remote, dao, rideDao, rideRequestDao)
            val user = SessionManager.user.value
            if (user == null) {
                Log.e("WORKER", "User is null")
                return Result.retry()
            }
            if (user.type== UserType.DRIVER){
                // A driver owns the rides they offer, so they roll their own
                // recurring rides forward to the next occurrence.
                rollOwnRides(rideDao, remote, user.id, now)

                val acceptedRideRequests = dao.getAcceptedRequests(user.id, nowString)
                Log.d("VALIDATION", "Validating RideRequests $acceptedRideRequests")
                acceptedRideRequests.forEach { request ->
                    repository.checkRequestStillValid(applicationContext, request)
                }
            }
            if (user.type== UserType.RIDER){
                // A rider owns the requests they create, so they roll their own
                // recurring requests forward.
                rollOwnRequests(rideRequestDao, remote, user.id, now)

                val subscribedRides = dao.getSubscribedRides(user.id)
                Log.d("VALIDATION", "Validating Rides $subscribedRides")
                subscribedRides.forEach { subscription->
                    repository.checkRideStillValid(applicationContext, subscription.subscription)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("VALIDATION", "Error Validating the Rides", e)
            Result.retry()
        }
    }

    // Next occurrence at or after `now`, computed from the start time so the
    // wall-clock time of day is preserved. Loops to catch up if the worker was
    // idle across several missed occurrences.
    private fun nextStart(start: Instant, isDaily: Boolean, now: Instant): Instant {
        val period = if (isDaily) 1.days else 7.days
        var next = start
        while (next <= now) next += period
        return next
    }

    private suspend fun rollOwnRides(
        rideDao: RideDao,
        remote: StorageRemoteDataSource,
        userId: String,
        now: Instant
    ) {
        val due = rideDao.getOwnRecurringRidesDue(userId, now.toString())
        due.forEach { ride ->
            val oldStart = ride.departureDateTime ?: return@forEach
            val newStart = nextStart(oldStart, ride.isDaily, now)
            // Shift arrival by the same amount to preserve the trip duration.
            val newArrival = ride.arrivalDateTime?.plus(newStart - oldStart)
            try {
                // Remote first: only update locally once the server confirms, so
                // a failure simply leaves the row "due" to retry next cycle.
                remote.rollRideDates(ride.id, newStart, newArrival)
                // syncState is left untouched: the DAO query only returns
                // SYNCED (server-backed) rows, so there is nothing to flip.
                rideDao.updateRide(
                    ride.copy(
                        departureDateTime = newStart,
                        arrivalDateTime = newArrival
                    )
                )
                Log.d("VALIDATION", "Rolled ride ${ride.id} to $newStart")
            } catch (e: Exception) {
                Log.e("VALIDATION", "Failed to roll ride ${ride.id}", e)
            }
        }
    }

    private suspend fun rollOwnRequests(
        rideRequestDao: RideRequestDao,
        remote: StorageRemoteDataSource,
        userId: String,
        now: Instant
    ) {
        val due = rideRequestDao.getOwnRecurringRequestsDue(userId, now.toString())
        due.forEach { request ->
            val newDate = nextStart(request.date, request.isDaily, now)
            try {
                remote.rollRideRequestDate(request.id, newDate)
                // syncState left untouched — DAO only returns SYNCED rows.
                rideRequestDao.updateRideRequest(
                    request.copy(date = newDate)
                )
                Log.d("VALIDATION", "Rolled request ${request.id} to $newDate")
            } catch (e: Exception) {
                Log.e("VALIDATION", "Failed to roll request ${request.id}", e)
            }
        }
    }
}
