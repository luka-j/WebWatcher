package rs.lukaj.utils.webwatcher

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.text.DecimalFormat
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Controller
class Controller {

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
            val watchers = HashMap<String, WatcherConfig>()
            watchers["form_000"] = WatcherConfig("Raspored form_0", base + "index.html", 8, 4)
            val decimalFormat = DecimalFormat("000")
            for(i in 1..43) {
                watchers["form_$i"] = WatcherConfig("Raspored form_$i", base + "form_" + decimalFormat.format(i) + ".html", 8, 4)
            }
            return ResponseEntity.ok(watchers)
        }
        return ResponseEntity.notFound().build()
    }
}