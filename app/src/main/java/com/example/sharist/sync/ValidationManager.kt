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

object ValidationManager {
    fun enqueueValidation(context: Context) {
        Log.d("WORKER", "Enqueuing periodic validation work")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<ValidationWorker>(
            15, TimeUnit.MINUTES // minimum allowed by WorkManager
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "validation_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    fun triggerImmediateValidation(context: Context) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<ValidationWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("validation_worker",
                ExistingWorkPolicy.KEEP,
                request)
    }
    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("validation_worker")
    }
}