package rs.lukaj.utils.webwatcher

import com.sendgrid.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException


@Component
object Executor {
    private val LOG : Logger = LoggerFactory.getLogger(Executor.javaClass)

    var pause = 0
    var watchers : Map<String, Watcher> = HashMap()
        private set
    private var firstLoad = true
    private var lastErrorNotification = 0L

    private val pendingChanges = ArrayList<Change>()

    @Scheduled(fixedRate = 1000 * 60, initialDelay = 1000 * 60)
    fun executeWatchers() {
        if(pause > 0) {
            pause--
            return
        }
        LOG.info("Updating watchers from config")
        updateWatchers()

        try {
            val changes = ArrayList<Change>()
            LOG.info("Running watchers")
            var hasMajorChanges = false
            for (watcher in watchers) {
                val change = watcher.value.hasChanged()
                if (change.changeType == ChangeType.MAJOR_CHANGE) {
                    changes.add(change)
                    hasMajorChanges = true
                } else if (change.changeType == ChangeType.MINOR_CHANGE) {
                    LOG.info("Adding a minor change to pending queue - $change")
                    pendingChanges.add(change)
                }
            }
            if (hasMajorChanges) handleChanges(changes)
            LOG.info("Finished running watchers and notifications sent")
        } catch(e: Exception) {
            LOG.error("Exception occurred while scraping", e)
            notifyError(e)
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 130)
    fun saveWatcherStates() {
        WatcherIO.saveStates(watchers.mapValues { v -> v.value.state })
    }

    @Scheduled(fixedDelay = 60 * 1000 * 45)
    private fun updateWatchers() {
        try {
            val newWatchers = WatcherIO.loadConfig().mapValues { v->Watcher(v.value, WatcherState()) }
            if(firstLoad) {
                val states = WatcherIO.loadStates()
                for(state in states) {
                    if(newWatchers.containsKey(state.key)) {
                        (newWatchers[state.key] ?: error("Mustn't happen")).state = state.value
                    } else {
                        LOG.warn("Unpaired Config/State, state id ${state.key}")
                    }
                }
                firstLoad = false
            } else {
                for (newWatcher in newWatchers) {
                    if (watchers.containsKey(newWatcher.key)) {
                        newWatcher.value.state = watchers[newWatcher.key]?.state ?: error("Mustn't happen (2)")
                        newWatcher.value.config.initialDelay = watchers[newWatcher.key]?.config?.initialDelay ?: error("Mustn't happen (2)")
                    }
                }
            }
            watchers = newWatchers
        } catch (e : Exception) {
            notifyError(e)
        }
    }

    private var lastNotificationSent = 0L
    private val notificationQueue = ArrayList<String>()
    var notificationsSilenced = 0
    private fun handleChanges(changes : MutableList<Change>) {
        if(changes.isEmpty()) return
        val changesNo = changes.size + pendingChanges.size

        val majorChanges = changesToString(changes)
        val minorChanges = changesToString(pendingChanges)
        pendingChanges.clear()
        val message = if(pendingChanges.isEmpty()) majorChanges else "$majorChanges<br><br>Other minor changes: $minorChanges"
        if(System.currentTimeMillis() - lastNotificationSent < Config.getChangeNotificationCooldown() || notificationsSilenced-- > 0) {
            notificationQueue.add(message)
        } else {
            lastNotificationSent = System.currentTimeMillis()
            if(notificationQueue.isEmpty()) {
                val subject = "$changesNo website(s) changed"
                sendMail(subject, message, true)
            } else {
                val subject = "Multiple websites changed"
                val aggregatedMessage = message + "<br><br>Aggregated notifications:<br>" + notificationQueue.joinToString("<br><br>")
                notificationQueue.clear()
                sendMail(subject, aggregatedMessage, true)
            }
        }
    }

    private fun changesToString(changes : List<Change>) = changes.stream()
            .sorted { c1, c2 -> c2.changeAmount - c1.changeAmount }
            .map { ch -> ch.toString() }
            .reduce { a, b -> "$a<br>$b" }
            .orElseGet { ""}

    private fun notifyError(e : Exception) {
        LOG.error("Unexpected error occurred", e)

        if(System.currentTimeMillis() - lastErrorNotification > Config.getErrorNotificationCooldown()) {
            sendMail("WebWatcher error!", "Exception occurred while scraping website! " + e.javaClass + ": " + e.message, false)
            lastErrorNotification = System.currentTimeMillis()
        }
    }

    internal fun sendMail(subject: String, body: String, isHtml : Boolean) {
        val from = Email(Config.getSendingEmailAddress())
        val to = Email(Config.getReceivingEmailAddress())
        val content = Content(if(isHtml) "text/html" else "text/plain", body)
        val mail = Mail(from, subject, to, content)

        val sg = SendGrid(Config.getSendgridApiKey())
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