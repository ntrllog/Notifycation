package ntrllog.github.io.notifycation

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val savedNotifications =
            applicationContext.getSharedPreferences("notifications", Context.MODE_PRIVATE)
        createNotificationChannel()
        loadNotificationsInDrawer(savedNotifications)
        return Result.success()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifycation"
            val description = "Channel for Notifycation"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("notifycation", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadNotificationsInDrawer(savedNotifications: SharedPreferences) {
        val gson = Gson()
        val keys = savedNotifications.all

        for ((_, value) in keys) {
            val json = value.toString()
            val notification = gson.fromJson(json, Notification::class.java)
            sendNotification(notification.content, notification.id, applicationContext)
        }
    }

    private fun sendNotification(s: String, i: Int, context: Context) {
        // create intent so app opens when tapping notification
        // Create an Intent for the activity you want to start
        val resultIntent = Intent(context, MainActivity::class.java)
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        // Get the PendingIntent containing the entire back stack
        val resultPendingIntent =
            stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        // create notification in notification drawer
        val mBuilder = NotificationCompat.Builder(context, "notifycation")
            .setSmallIcon(R.drawable.ic_announcement_black_24dp)
            .setContentTitle("Notifycation")
            .setContentText(s)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(s + i)
            .setContentIntent(resultPendingIntent)
        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
                999
            )
            return
        }
        notificationManager.notify(i, mBuilder.build())
    }
}