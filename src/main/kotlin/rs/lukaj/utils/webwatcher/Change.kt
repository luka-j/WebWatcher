package rs.lukaj.utils.webwatcher

data class Change(val watcher : Watcher, val hasChanged: Boolean, val changeAmount : Int)

fun noChange(watcher : Watcher) : Change {
    return Change(watcher, false, 0)
}