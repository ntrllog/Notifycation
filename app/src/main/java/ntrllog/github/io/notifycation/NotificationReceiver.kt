package ntrllog.github.io.notifycation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// this is for receiving the boot completed event and then running the app
// on some devices, auto launch may need to be enabled for this app (in the settings)
// it may take a few minutes for boot to be considered completed
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, NotificationService::class.java)
            NotificationService.enqueueWork(context, serviceIntent)
        }
    }
}