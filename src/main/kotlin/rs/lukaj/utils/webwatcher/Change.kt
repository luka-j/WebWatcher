package rs.lukaj.utils.webwatcher

data class Change(val watcher : Watcher, val changeType: ChangeType, val changeAmount : Int, val lastChangeTime : Long, val currentChangeTime : Long) {
    override fun toString(): String =
            watcher.config.name + ": " + changeAmount + " bytes " +
                    "(<a href=\"${buildStateLink(watcher.config.name, lastChangeTime)}\">old</a> - " +
                    "<a href=\"${buildStateLink(watcher.config.name, currentChangeTime)}\">new</a>)"
}

fun noChange(watcher : Watcher) : Change {
    return Change(watcher, ChangeType.NO_CHANGE, 0, watcher.state.history.last().first, watcher.state.history.last().first)
}

fun buildStateLink(name: String, time: Long) : String = System.getProperty(HOST_PROPERTY) + "/state?name=$name&time=$time"

enum class ChangeType {
    NO_CHANGE,
    MINOR_CHANGE,
    MAJOR_CHANGE
}