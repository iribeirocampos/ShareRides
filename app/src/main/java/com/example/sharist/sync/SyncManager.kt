package com.example.sharist.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager


object SyncManager {
    fun enqueueSync(context: Context) {
        Log.d("WORKER", "Enqueuing Work")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "sync_worker",
                ExistingWorkPolicy.KEEP,
                request
            )
    }
    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("sync_worker")
    }
}