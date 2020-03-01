package rs.lukaj.utils.webwatcher

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.stream.Collectors

object WatcherIO {
    private val LOG : Logger = LoggerFactory.getLogger(WatcherIO.javaClass)
    private val mapper = jacksonObjectMapper()

    fun loadConfig() : Map<String, WatcherConfig> {
        val configFile = File(Config.getWatcherConfigPath())
        LOG.info("Loading config from {}", configFile.absolutePath)
        return mapper.readValue<List<WatcherConfig>>(configFile, object : TypeReference<List<WatcherConfig>>() {})
                .stream().collect(Collectors.toMap({w->w.name}, {w->w}))
    }

    fun saveStates(states : Map<String, WatcherState>) {
        val stateFile = File(Config.getWatcherStatesPath())
        LOG.info("Saving states to {}", stateFile.absolutePath)
        mapper.writeValue(stateFile, states)
    }

    fun loadStates() : Map<String, WatcherState> {
        val stateFile = File(Config.getWatcherStatesPath())
        if(!stateFile.exists()) return HashMap()
        LOG.info("Loading states from {}", stateFile.absolutePath)
        return mapper.readValue<Map<String, WatcherState>>(stateFile, object : TypeReference<Map<String, WatcherState>>() {})
    }
}