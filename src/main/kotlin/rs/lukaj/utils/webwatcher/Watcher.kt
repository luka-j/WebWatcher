package rs.lukaj.utils.webwatcher

import org.jsoup.Jsoup
import rs.lukaj.utils.webwatcher.data.WatcherHistoryTable
import rs.lukaj.utils.webwatcher.data.WatcherStatesTable
import kotlin.collections.ArrayList

class Watcher(var config : WatcherConfig) {
    val state : WatcherState
        get() {
            return WatcherStatesTable.getState(config.name)
        }

    fun hasChanged() : Change {
        if((System.currentTimeMillis() - state.lastInvocation + 10)/MS_IN_MIN < config.period) return noChange(this)
        if(config.initialDelay > 0) {config.initialDelay -= 1; return noChange(this)}

        val prev = state.current
        val lastChangeTime = scrape()
        val currentChangeTime = WatcherHistoryTable.getLastChangeTime(config.name)
        val changeAmount = state.current.distance(prev)
        return if(changeAmount == 0) noChange(this)
        else {
            if(changeAmount > config.sensitivity) {
                Change(this, ChangeType.MAJOR_CHANGE, changeAmount, lastChangeTime, currentChangeTime)
            } else {
                Change(this, ChangeType.MINOR_CHANGE, changeAmount, lastChangeTime, currentChangeTime)
            }
        }
    }

    private fun scrape() : Long {
        val lastChange = WatcherHistoryTable.getLastChangeTime(config.name)
        val document = Jsoup.connect(config.url).get()
        val currentHTML = document.outerHtml()
        val currentText = document.body().text()
        WatcherStatesTable.setCurrentState(config.name, currentText, currentHTML)
        return lastChange
    }

    fun getStateAt(timeMs : Long) : String {
        return WatcherHistoryTable.getStateAt(config.name, timeMs)
    }

    fun hasHistory() : Boolean = WatcherHistoryTable.getLastState(config.name).isNotEmpty()

    fun getLastChangeTime() : Long = WatcherHistoryTable.getLastChangeTime(config.name)
}

data class WatcherConfig(var name: String, var url: String, var sensitivity : Int = 10, var period : Int = 15, var initialDelay : Int = 0)

data class WatcherState(var current : String = "", var currentHTML : String = "",
                        var history: ArrayList<Pair<Long, String>> = ArrayList(), var lastInvocation : Long = 0)