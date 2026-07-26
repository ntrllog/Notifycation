package ntrllog.github.io.notifycation

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.util.UUID
import kotlin.math.abs
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    private val notificationArrayList = ArrayList<Notification>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var listView: ListView
    private lateinit var savedNotifications: SharedPreferences // {notification_id = {"content": String, "id": String}}

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // apply status bar inset as top padding on the toolbar so it sits
        // below the status bar instead of underneath it (targetSdk 35+
        // enforces edge-to-edge, so we handle this ourselves)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, systemBars.top, view.paddingRight, view.paddingBottom)
            insets
        }

        val rootView = findViewById<View>(R.id.main_root)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, view.paddingTop, systemBars.right, systemBars.bottom)
            insets
        }

        savedNotifications = getSharedPreferences("notifications", MODE_PRIVATE)
        adapter = NotificationAdapter(this, notificationArrayList)
        listView = findViewById(R.id.list)
        registerForContextMenu(listView)

        // show notifications in ui
        updateAdapter()

        requestNotificationPermission()

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val id = generateUniqueId()
            notificationArrayList.add(Notification("Tap To Edit/Hold To Delete", id))
            listView.adapter = adapter
        }

        // show notifications in notification bar
        val notificationServiceIntent = Intent(this, NotificationService::class.java)
        NotificationService.enqueueWork(applicationContext, notificationServiceIntent)
    }

    private fun updateAdapter() {
        val gson = Gson()

        val keys = savedNotifications.all
        for ((_, value) in keys) {
            val json = value.toString()
            val notification = gson.fromJson(json, Notification::class.java)
            notificationArrayList.add(notification)
        }
        listView.adapter = adapter

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val notification = notificationArrayList[position]
                val customDialog = CustomDialog(this@MainActivity, notification.content)
                customDialog.setDialogResult(object : CustomDialog.OnMyDialogResult {
                    override fun finish(result: String?) {
                        if (result != null) {
                            notification.content = result
                        }

                        savedNotifications.edit {
                            val json = gson.toJson(notification)
                            putString(notification.id.toString(), json)
                        }

                        val notificationServiceIntent = Intent(
                            applicationContext, NotificationService::class.java
                        )
                        NotificationService.enqueueWork(
                            applicationContext,
                            notificationServiceIntent
                        )
                    }
                })
                customDialog.show()
            }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0, v.id, 0, "Delete")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo?

        // remove from shared preferences
        val notification = notificationArrayList[info!!.position]
        savedNotifications.edit {
            remove(notification.id.toString())
        }

        // remove from notification bar
        NotificationService.removeNotification(
            applicationContext,
            notification.id
        )

        // remove from ui
        notificationArrayList.removeAt(info.position)
        listView.adapter = adapter

        return true
    }

    private fun generateUniqueId(): Int {
        val timestamp = System.currentTimeMillis()
        val uniqueDeviceId = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
        return abs(timestamp xor uniqueDeviceId).toInt()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 999
    }
}