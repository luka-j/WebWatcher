package rs.lukaj.utils.webwatcher

object Config {
    private const val HOST_PROPERTY = "app.host"
    private const val DEFAULT_HOST = "localhost:8080"

    private const val SENDGRID_APIKEY_PROPERTY = "sendgrid.apikey"

    private const val FROM_EMAIL_PROPERTY = "email.sender"
    private val DEFAULT_FROM_EMAIL = "watcher@" + getHost()

    private const val RECIPIENT_EMAIL_PROPERTY = "email.recipient"

    private const val CONFIG_PATH_PROPERTY = "config.path"
    private const val DEFAULT_CONFIG_PATH = "watcherConfig.json"

    private const val STATES_PATH_PROPERTY = "states.path"
    private const val DEFAULT_STATES_PATH = "watcherStates.json"

    private const val PAUSE_EMAIL_COOLDOWN_PROPERTY = "pause.cooldown"
    private const val DEFAULT_PAUSE_EMAIL_COOLDOWN = 1000*60*60

    private const val PAUSE_KEY_LENGTH_PROPERTY = "pause.key.length"
    private const val DEFAULT_PAUSE_KEY_LENGTH = 192

    private const val ERROR_NOTIFICATION_COOLDOWN_PROPERTY = "email.error.cooldown"
    private const val DEFAULT_ERROR_NOTIFICATION_COOLDOWN = 1000*60*60
    private const val CHANGE_NOTIFICATION_COOLDOWN_PROPERTY = "email.change.cooldown"
    private const val DEFAULT_CHANGE_NOTIFICATION_COOLDOWN = 1000*60*5

    fun getHost() : String = System.getProperty(HOST_PROPERTY, DEFAULT_HOST)
    fun getSendgridApiKey() = System.getProperty(SENDGRID_APIKEY_PROPERTY)!!
    fun getSendingEmailAddress() : String = System.getProperty(FROM_EMAIL_PROPERTY, DEFAULT_FROM_EMAIL)
    fun getReceivingEmailAddress() = System.getProperty(RECIPIENT_EMAIL_PROPERTY)!!
    fun getWatcherConfigPath() : String = System.getProperty(CONFIG_PATH_PROPERTY, DEFAULT_CONFIG_PATH)
    fun getWatcherStatesPath() : String = System.getProperty(STATES_PATH_PROPERTY, DEFAULT_STATES_PATH)
    fun getPauseEmailCooldown() : Long = System.getProperty(PAUSE_EMAIL_COOLDOWN_PROPERTY,
            DEFAULT_PAUSE_EMAIL_COOLDOWN.toString()).toLong()
    fun getPauseKeyLength() : Int = System.getProperty(PAUSE_KEY_LENGTH_PROPERTY, DEFAULT_PAUSE_KEY_LENGTH.toString()).toInt()
    fun getErrorNotificationCooldown() : Long = System.getProperty(ERROR_NOTIFICATION_COOLDOWN_PROPERTY,
            DEFAULT_ERROR_NOTIFICATION_COOLDOWN.toString()).toLong()
    fun getChangeNotificationCooldown() : Long = System.getProperty(CHANGE_NOTIFICATION_COOLDOWN_PROPERTY,
            DEFAULT_CHANGE_NOTIFICATION_COOLDOWN.toString()).toLong()
}