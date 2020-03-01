package rs.lukaj.utils.webwatcher

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AdminAuth {
    private val LOG : Logger = LoggerFactory.getLogger(AdminAuth.javaClass)

    private val keys = HashMap<String, String>()

    fun sendAuth(action: String, path: String) {
        val key = randomString(Config.getAuthKeyLength())
        keys[action] = key
        Executor.sendMail("WebWatcher $action link", "<a href=\"${Config.getHost()}$path&key=$key\">$action Watcher</a>", true)
    }

    fun isKeyValid(action: String, key: String, ipAddr: String) : Boolean {
        if(!keys.containsKey(action)) {
            LOG.warn("Attempted to authenticate for unknown action ($action)")
            return false
        }
        val success = keys[action] == key
        if(!success) {
            LOG.warn("Missed $action key! Attempted $key from $ipAddr")
        }
        keys.remove(action)
        return success
    }
}