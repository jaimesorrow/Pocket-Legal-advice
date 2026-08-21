package com.pocketlawbook.alaska.data.legal

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "pocket-lawbook-daily-legal-content-refresh"

/** Schedules the best-effort daily freshness check allowed by Android. */
object LegalContentRefreshScheduler {
    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<LegalContentRefreshWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}

/**
 * Worker shell for the production sync implementation.
 * The repository implementation is injected by the app's DI layer before
 * this worker is wired into production.
 */
class LegalContentRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // Wiring is intentionally delegated to LegalContentSyncRepository.
        // Until a verified backend is configured, never replace the local
        // known-good dataset with network data.
        return Result.success()
    }
}
