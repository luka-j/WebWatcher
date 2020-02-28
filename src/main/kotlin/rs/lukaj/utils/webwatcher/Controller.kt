package rs.lukaj.utils.webwatcher

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

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
}