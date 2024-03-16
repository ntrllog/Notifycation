package ntrllog.github.io.notifycation

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class NotificationAdapter(
    context: Activity?,
    notifications: ArrayList<Notification>
) : ArrayAdapter<Notification>(
    context!!, 0, notifications
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }
        val currentNotification = getItem(position)
        val content = listItemView!!.findViewById<TextView>(R.id.content)
        content.text = currentNotification!!.content
        return listItemView
    }
}