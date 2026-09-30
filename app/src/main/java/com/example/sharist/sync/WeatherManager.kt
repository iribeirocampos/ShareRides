package com.example.sharist.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WeatherSyncManager {
    fun enqueueWeatherSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<WeatherWorker>(
            12, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "weather_worker",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun triggerImmediateWeather(context: Context) {
        Log.d("WEATHER", "Runing Weather trigger once")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<ValidationWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("weather_worker",
                ExistingWorkPolicy.KEEP,
                request)
    }
    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("weather_worker")
    }
}