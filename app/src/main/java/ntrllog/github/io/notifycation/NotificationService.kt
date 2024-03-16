package ntrllog.github.io.notifycation

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class NotificationService {
    companion object {
        fun enqueueWork(context: Context, work: Intent?) {
            val notificationWorkRequest =
                OneTimeWorkRequest.Builder(NotificationWorker::class.java).build()
            WorkManager.getInstance(context).enqueue(notificationWorkRequest)
        }

        fun removeNotification(context: Context, id: Int) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(id)
        }
    }
}