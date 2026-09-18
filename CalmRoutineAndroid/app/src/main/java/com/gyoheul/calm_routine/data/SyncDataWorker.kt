package com.gyoheul.calm_routine.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncDataWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val backupManager = BackupManager(applicationContext)
            val syncSuccess = backupManager.syncUserData()
            if (syncSuccess) {
                val backupSuccess = backupManager.backupUserData()
                if (backupSuccess) {
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
