package rs.lukaj.utils.webwatcher

data class Change(val watcher : Watcher, val changeType: ChangeType, val changeAmount : Int) {
    override fun toString(): String {
        return watcher.config.name + ": " + changeAmount + " bytes"
    }
}

fun noChange(watcher : Watcher) : Change {
    return Change(watcher, ChangeType.NO_CHANGE, 0)
}

enum class ChangeType {
    NO_CHANGE,
    MINOR_CHANGE,
    MAJOR_CHANGE
}