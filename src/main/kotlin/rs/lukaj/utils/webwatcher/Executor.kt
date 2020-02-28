package rs.lukaj.utils.webwatcher

import com.sendgrid.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException


const val SENDGRID_APIKEY_PROPERTY = "sendgrid.apikey"
const val RECIPIENT_EMAIL_PROPERTY = "email.recipient"

private val LOG : Logger = LoggerFactory.getLogger(Executor.javaClass)
@Component
object Executor {
    var watchers : Map<String, Watcher> = HashMap()
        private set
    private var firstLoad = true
    private var lastErrorNotification = 0L

    private val pendingChanges = ArrayList<Change>()

    @Scheduled(fixedRate = 1000 * 60, initialDelay = 1000)
    fun executeWatchers() {
        LOG.info("Updating watchers from config")
        updateWatchers()

        val changes = ArrayList<Change>()
        LOG.info("Running watchers")
        var hasMajorChanges = false
        for(watcher in watchers) {
            val change = watcher.value.hasChanged()
            if(change.changeType == ChangeType.MAJOR_CHANGE) {
                changes.add(change)
                hasMajorChanges = true
            } else {
                pendingChanges.add(change)
            }
        }
        if(hasMajorChanges) handleChanges(changes)
        LOG.info("Finished running watchers and notifications sent")
    }

    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 65)
    fun saveWatcherStates() {
        WatcherIO.saveStates(watchers.mapValues { v -> v.value.state })
    }

    private fun updateWatchers() {
        try {
            val newWatchers = WatcherIO.loadConfig().mapValues { v->Watcher(v.value, WatcherState()) }
            if(firstLoad) {
                val states = WatcherIO.loadStates()
                for(state in states) {
                    (newWatchers[state.key] ?: error("Config/State not correctly paired!")).state = state.value
                }
                firstLoad = false
            } else {
                for (newWatcher in newWatchers) {
                    if (watchers.containsKey(newWatcher.key)) {
                        newWatcher.value.state = watchers[newWatcher.key]?.state ?: error("Mustn't happen")
                    }
                }
            }
            watchers = newWatchers
        } catch (e : Exception) {
            notifyError(e)
        }
    }

    private fun handleChanges(changes : MutableList<Change>) {
        if(changes.isEmpty()) return
        val changesNo = changes.size + pendingChanges.size

        val majorChanges = changesToString(changes)
        val minorChanges = changesToString(pendingChanges)
        pendingChanges.clear()
        val message = if(pendingChanges.isEmpty()) majorChanges else "$majorChanges\n\nOther minor changes: $minorChanges"
        val subject = "$changesNo website(s) changed"
        sendMail(subject, message)
    }

    private fun changesToString(changes : List<Change>) = changes.stream()
            .sorted { c1, c2 -> c2.changeAmount - c1.changeAmount }
            .map { ch -> ch.toString() }
            .reduce { a, b -> a+"\n"+b}
            .orElseGet { ""}

    private fun notifyError(e : Exception) {
        LOG.error("Unexpected error occurred", e)

        if(System.currentTimeMillis() - lastErrorNotification > 1000 * 60 * 60) {
            sendMail("WebWatcher error!", "Exception ocurred while scraping website! " + e.javaClass + ": " + e.message)
            lastErrorNotification = System.currentTimeMillis()
        }
    }

    private fun sendMail(subject: String, body: String) {
        val from = Email("watcher@webwatcher.ml")
        val to = Email(System.getProperty(RECIPIENT_EMAIL_PROPERTY))
        val content = Content("text/plain", body)
        val mail = Mail(from, subject, to, content)

        val sg = SendGrid(System.getProperty(SENDGRID_APIKEY_PROPERTY))
        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response: Response = sg.api(request)
            LOG.info("Sent email! Sendgrid status " + response.statusCode)
        } catch (ex: IOException) {
            LOG.error("Error while sending email!", ex)
        }
    }
}