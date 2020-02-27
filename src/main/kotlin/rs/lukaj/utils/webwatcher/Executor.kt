package rs.lukaj.utils.webwatcher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val LOG : Logger = LoggerFactory.getLogger(Executor.javaClass)
@Component
object Executor {
    var watchers : Map<String, Watcher> = HashMap()
        private set

    @Scheduled(fixedRate = 1000 * 60, initialDelay = 1)
    fun executeWatchers() {
        LOG.info("Updating watchers from config")
        updateWatchers()

        val changes = ArrayList<Change>()
        LOG.info("Running watchers")
        for(watcher in watchers) {
            val change = watcher.value.hasChanged()
            if(change.hasChanged) changes.add(change)
        }
        handleChanges(changes)
        LOG.info("Finished running watchers and notifications sent")
    }

    private fun updateWatchers() {
        try {
            val newWatchers = ConfigLoader.loadConfig()
            for(newWatcher in newWatchers) {
                if(watchers.containsKey(newWatcher.key)) {
                    newWatcher.value.copyState(watchers[newWatcher.key] ?: error("mustn't happen"))
                }
            }
            watchers = newWatchers
        } catch (e : Exception) {
            notifyError(e)
        }
    }

    private fun handleChanges(changes : List<Change>) {
        for(change in changes) {
            LOG.info("Page {} changed by {}", change.watcher.name, change.changeAmount)
        }
        //todo send mail or whatever
    }

    private fun notifyError(e : Exception) {
        LOG.error("Unexpected error occurred", e)
        //todo send mail or whatever
    }
}