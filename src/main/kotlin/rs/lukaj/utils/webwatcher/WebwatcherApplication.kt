package rs.lukaj.utils.webwatcher

import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WebwatcherApplication

fun main(args: Array<String>) {
    Database.connect("jdbc:postgresql://localhost:5432/webwatcher", "org.postgresql.Driver",
            getProperty("app.db.user", "luka"), getProperty("app.db.pass", "appDbAdmin"))
    runApplication<WebwatcherApplication>(*args)
}
