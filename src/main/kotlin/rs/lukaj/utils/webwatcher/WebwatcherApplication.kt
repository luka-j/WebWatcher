package rs.lukaj.utils.webwatcher

import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WebwatcherApplication

fun main(args: Array<String>) {
    Database.connect("jdbc:postgresql://localhost:5432/webwatcher", "org.postgresql.Driver", "luka", "appDbAdmin")
    runApplication<WebwatcherApplication>(*args)
}
