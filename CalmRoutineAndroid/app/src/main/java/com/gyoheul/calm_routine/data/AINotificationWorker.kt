package com.gyoheul.calm_routine.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.model.ProfileData

class AINotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val preferencesManager = SharedPreferencesManager(context)
            val notificationTypes: Set<EnumClass.NotificationType> = preferencesManager.getNotificationTypes()

            if (!notificationTypes.contains(EnumClass.NotificationType.AI)) {
                return Result.success()
            }

            val sqLiteManager = SQLiteManager(context)
            val style = preferencesManager.getAICommentStyle(EnumClass.AICommentStyle.Friendly)

            val routineMap = sqLiteManager.getRoutinesWithDateGroup(limit = 5)
            val profileData: ProfileData? = preferencesManager.getSettingsProfileData()
            val response = OpenAIManager().sendMessageToChatGPT(
                buildList {
                    add(AICommentModel.getAICoachingSystemPrompt(style, routineMap, profileData))
                    add(AICommentModel.getAICoachingMessage(style))
                }
            ) // ← GPT API リクエスト
            if (response?.content?.isNotBlank() == true) {
                val notificationData = NotificationModel.getAINotificationData(response.content)
                return notificationData?.let {
                    NotificationModel.showNotification(context, it)
                    Result.success()
                } ?: Result.retry()
            }
            Result.retry()
        } catch (e: Exception) {
            LogModel.e(this, "AI Notification worker failed", e)
            // 最大リトライ回数を確認
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
