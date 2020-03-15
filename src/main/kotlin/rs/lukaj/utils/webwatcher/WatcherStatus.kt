package rs.lukaj.utils.webwatcher

class WatcherStatus(watcher : Watcher) {
    var name = ""
        private set
    var lastInvocation = 0L
        private set
    var lastChange = 0L
        private set

    init {
        name = watcher.config.name
        lastInvocation = watcher.state.lastInvocation
        lastChange = if(!watcher.hasHistory()) 0 else watcher.getLastChangeTime()
    }
}