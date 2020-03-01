package rs.lukaj.utils.webwatcher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.text.DecimalFormat
import javax.servlet.http.HttpServletRequest
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@Controller
class WatcherController {
    companion object {
        private val LOG : Logger = LoggerFactory.getLogger(WatcherController::class.java)
    }

    @GetMapping("/status")
    fun status() : ResponseEntity<Any> {
        return ResponseEntity.ok(Executor.watchers.map { watcher -> WatcherStatus(watcher.value) }.toCollection(ArrayList()))
    }

    @GetMapping("/state")
    fun stateAt(@RequestParam name : String, @RequestParam time : Long) : ResponseEntity<Any> {
        val watcher = Executor.watchers[name] ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(watcher.getStateAt(time))
    }

    @GetMapping("/generate")
    fun generateConfig(@RequestParam name : String) : ResponseEntity<Any> {
        if(name == "rasporedi") {
            val base = "http://poincare.matf.bg.ac.rs/~kmiljan/raspored/sve/"
            val watchers = ArrayList<WatcherConfig>()
            watchers.add(WatcherConfig("Raspored form_0", base + "index.html", 8, 88))
            val decimalFormat = DecimalFormat("000")
            for(i in 1..43) {
                watchers.add(WatcherConfig("Raspored form_$i", base + "form_" + decimalFormat.format(i) + ".html",
                        8, 88, (i%44)*2))
            }
            return ResponseEntity.ok(watchers)
        }
        return ResponseEntity.notFound().build()
    }

    private var lastPauseAttempt : Long = 0
    @GetMapping("/pause")
    fun pause(@RequestParam(defaultValue = "") key : String, @RequestParam time : Int, request : HttpServletRequest) : ResponseEntity<Any> {
        LOG.info("Attempting pause with key: $key from ${request.remoteAddr}")
        //this is a bit simplistic but should be reasonably secure (and non-spammy)
        if(StringUtils.isEmpty(key)) {
            if (System.currentTimeMillis() - lastPauseAttempt < Config.getPauseEmailCooldown()) {
                LOG.info("Hitting pause endpoint too often!")
                return ResponseEntity.ok("Sent confirmation link!")
            }
            lastPauseAttempt = System.currentTimeMillis()
            AdminAuth.sendAuth("pause", "<a href=\"${Config.getHost()}/pause?key=$key&time=$time\">Pause Watcher</a>")
            return ResponseEntity.ok("Sent confirmation link!")
        } else {
            if(AdminAuth.checkAuth("pause", key, request.remoteAddr)) {
                lastPauseAttempt = System.currentTimeMillis()
            } else {
                Executor.pause = time
                lastPauseAttempt = 0
                LOG.info("Paused Watcher for $time cycles")
            }
            return ResponseEntity.ok("Paused!")
        }
    }

    private var lastSilenceAttempt : Long = 0
    @GetMapping("/silence")
    fun silence(@RequestParam(defaultValue = "") key : String, @RequestParam time : Int, request: HttpServletRequest) : ResponseEntity<Any> {
        LOG.info("Attempting silence with key: $key from ${request.remoteAddr}")
        //this is a bit simplistic but should be reasonably secure (and non-spammy)
        if(StringUtils.isEmpty(key)) {
            if (System.currentTimeMillis() - lastSilenceAttempt < Config.getSilenceEmailCooldown()) {
                LOG.info("Hitting silence endpoint too often!")
            } else {
                lastSilenceAttempt = System.currentTimeMillis()
                AdminAuth.sendAuth("silence", "<a href=\"${Config.getHost()}/silence?key=$key&time=$time\">Silence Watcher</a>")
            }
            return ResponseEntity.ok("Sent confirmation link!")
        } else {
            if(AdminAuth.checkAuth("silence", key, request.remoteAddr)) {
                lastSilenceAttempt = System.currentTimeMillis()
            } else {
                Executor.notificationsSilenced = time
                lastSilenceAttempt = 0
                LOG.info("Silenced Watcher for $time cycles")
            }
            return ResponseEntity.ok("Silenced!")
        }
    }
}