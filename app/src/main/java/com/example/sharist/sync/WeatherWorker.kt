package com.example.sharist.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sharist.data.local.database.SharistDatabase
import com.example.sharist.data.model.isCancellableNow
import com.example.sharist.data.remote.IpmaApi
import com.example.sharist.data.remote.StorageRemoteDataSource
import com.example.sharist.data.repository.SubscriptionRepository
import com.example.sharist.data.session.SessionManager
import com.example.sharist.ui.components.events.UiEvent
import com.example.sharist.ui.components.events.UiEventBus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class WeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("WEATHER", "Running Weather Check")
            val db = SharistDatabase.getDatabase(applicationContext)
            val dao = db.subscriptionDao()
            val rideDao = db.rideDao()
            val rideRequestDao = db.rideRequestDao()
            val remote = StorageRemoteDataSource()
            val repository = SubscriptionRepository(remote, dao, rideDao, rideRequestDao)
            val user = SessionManager.user.value
            if (user == null) {
                Log.e("WORKER", "User is null")
                return Result.retry()
            }
            val subscribedRides = dao.getSubscribedRidesWeather(user.id)
            subscribedRides.forEach { subscribedRide ->
                Log.d("WORKER", "RideId: ${subscribedRide.ride.id}")
                // check weather on departure location
                val weather = IpmaApi.getForecast(
                    latitude = subscribedRide.ride.departure.latitude,
                    longitude = subscribedRide.ride.departure.longitude
                )
                val targetDate = subscribedRide.ride.departureDateTime!!
                    .toLocalDateTime(TimeZone.of("Europe/Lisbon"))
                    .date
                    .toString()
                val forecastForDay = weather.data.find { forecast ->
                    forecast.forecastDate == targetDate
                }
                if (forecastForDay==null || forecastForDay.precipitaProb==null){
                    return@forEach
                }
                if ((forecastForDay.precipitaProb.toIntOrNull()?:0) < 40){
                    // Rain probability is Low Cancel Ride
                    if (subscribedRide.ride.isCancellableNow()){
                        repository.unsubscribeWeatherRide(subscribedRide)
                    }else{
                        UiEventBus.send(UiEvent.Warning("Your subscribed Ride departing ${subscribedRide.ride.departure.address} is not raining. However, cannot cancel, Too late "))
                        val updatedSubscription = subscribedRide.subscription.copy(onlyInRain = false)
                        dao.insertSubscription(updatedSubscription)
                    }
                }
            }
            Log.d("WEATHER", "Weather updated")
            Result.success()
        } catch (e: Exception) {
            Log.e("WEATHER", "Error fetching weather", e)
            Result.retry()
        }
    }
}