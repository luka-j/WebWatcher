package rs.lukaj.utils.webwatcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WebwatcherApplication

fun main(args: Array<String>) {
    runApplication<WebwatcherApplication>(*args)
}
