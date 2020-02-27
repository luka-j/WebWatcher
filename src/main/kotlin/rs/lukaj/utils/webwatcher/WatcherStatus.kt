package rs.lukaj.utils.webwatcher

class WatcherStatus(private val watcher : Watcher) {
    var name = ""
        private set
    var lastInvocation = 0L
        private set

    init {
        name = watcher.name
        lastInvocation = watcher.lastInvocation
    }
}