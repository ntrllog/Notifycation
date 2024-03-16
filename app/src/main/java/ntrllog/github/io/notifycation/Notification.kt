package ntrllog.github.io.notifycation

class Notification(var content: String, val id: Int) {

    override fun toString(): String {
        return "$id: $content"
    }
}