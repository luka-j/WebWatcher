package rs.lukaj.utils.webwatcher

import org.jsoup.Jsoup
import kotlin.collections.ArrayList

const val MS_IN_MIN = 60000
class Watcher(var config : WatcherConfig, var state : WatcherState) {

    fun hasChanged() : Change {
        if((System.currentTimeMillis() - state.lastInvocation + 10)/MS_IN_MIN < config.period) return noChange(this)

        val prev = state.current
        state.current = scrape()
        val changeAmount = state.current.distance(prev)
        return if(changeAmount == 0) noChange(this)
        else {
            state.history.add(Pair(state.lastInvocation, state.currentHTML))
            if(changeAmount > config.sensitivity) {
                Change(this, ChangeType.MAJOR_CHANGE, changeAmount)
            } else {
                Change(this, ChangeType.MINOR_CHANGE, changeAmount)
            }
        }
    }

    private fun scrape() : String {
        val document = Jsoup.connect(config.url).get()
        state.currentHTML = document.outerHtml()
        state.lastInvocation = System.currentTimeMillis()
        return document.body().text()
    }

    fun getStateAt(timeMs : Long) : String {
        return binarySearchTime(state.history, timeMs).second
    }
}

data class WatcherConfig(var name: String, var url: String, var sensitivity : Int = 10, var period : Int = 15)

data class WatcherState(var current : String = "", var currentHTML : String = "",
                        var history: ArrayList<Pair<Long, String>> = ArrayList(), var lastInvocation : Long = 0)

fun binarySearchTime(input: ArrayList<Pair<Long, String>>, timeToSearch: Long) : Pair<Long, String> {
    var low = 0
    var high = input.size-1
    var mid = 0
    while(low <= high) {
        mid = (low + high)/2
        when {
            timeToSearch >input[mid].first   -> low = mid+1
            timeToSearch == input[mid].first -> return input[mid]
            timeToSearch < input[mid].first  -> high = mid-1
        }
    }
    return input[mid]
}