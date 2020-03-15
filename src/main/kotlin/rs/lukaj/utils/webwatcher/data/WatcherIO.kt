package rs.lukaj.utils.webwatcher.data

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rs.lukaj.utils.webwatcher.Config
import rs.lukaj.utils.webwatcher.WatcherConfig
import java.io.File
import java.util.stream.Collectors

object WatcherIO {
    private val LOG : Logger = LoggerFactory.getLogger(WatcherIO.javaClass)
    private val mapper = jacksonObjectMapper()

    fun loadConfig() : Map<String, WatcherConfig> {
        val configFile = File(Config.getWatcherConfigPath())
        return mapper.readValue(configFile, object : TypeReference<List<WatcherConfig>>() {})
                .stream().collect(Collectors.toMap({w->w.name}, {w->w}))
    }
}