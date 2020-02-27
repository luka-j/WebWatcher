package rs.lukaj.utils.webwatcher

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

private val CONFIG_PATH = "webpages.json"
private val LOG : Logger = LoggerFactory.getLogger(ConfigLoader.javaClass)
private val mapper = jacksonObjectMapper()

object ConfigLoader {

    fun loadConfig() : Map<String, Watcher> {
        val configFile = File(CONFIG_PATH)
        LOG.info("Loading config from {}", configFile.absolutePath)
        return mapper.readValue<Map<String, Watcher>>(configFile, object : TypeReference<Map<String, Watcher>>() {})
    }
}