package rs.lukaj.utils.webwatcher

import com.fasterxml.jackson.annotation.JsonIgnore
import org.jsoup.Jsoup

class Watcher(var name: String, var url: String, var sensitivity : Int = 10, var period : Int = 1000*60*15) {
    private var history = LinkedHashMap<Long, String>()

    @JsonIgnore
    private var current : String = ""
    @JsonIgnore
    private var currentHTML : String = ""
    @JsonIgnore
    var lastInvocation : Long = 0
        private set

    fun hasChanged() : Change {
        if(System.currentTimeMillis() - lastInvocation < period) return noChange(this)

        val prev = current
        current = scrape()
        val changeAmount = current.distance(prev)
        val hasChanged = changeAmount > sensitivity
        if(hasChanged) history[lastInvocation] = currentHTML
        return Change(this, hasChanged, changeAmount)
    }

    private fun scrape() : String {
        val document = Jsoup.connect(url).get()
        currentHTML = document.outerHtml()
        lastInvocation = System.currentTimeMillis()
        return document.body().text()
    }

    fun copyState(from : Watcher) {
        history = from.history
        current = from.current
        lastInvocation = from.lastInvocation
    }

    fun getStateAt(timeMs : Long) : String {
        var state = ""
        for(entry in history) {
            if(entry.key > timeMs) return state
            state = entry.value
        }
        return state
    }
}