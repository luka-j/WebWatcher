package rs.lukaj.utils.webwatcher.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.and

object WatcherHistoryTable : Table() {
    val watcherName = text("name")
    val time = long("time")
    val html = text("html")
    val text = text("text")

    override val primaryKey = PrimaryKey(watcherName, time, name="PKConstraint")

    init {
        transaction {
            SchemaUtils.create(WatcherHistoryTable)
        }
    }

    fun getStateAt(name : String, time : Long) : String {
        var state = ""
        transaction {
            WatcherHistoryTable.select { (watcherName eq name) and (WatcherHistoryTable.time lessEq time) }
                    .orderBy(WatcherHistoryTable.time to SortOrder.DESC)
                    .limit(1)
                    .map { state = it[html] }
        }
        return state
    }

    fun getLastChangeTime(name : String) : Long {
        var retTime = 0L
        transaction {
            WatcherHistoryTable.select { (watcherName eq name) }
                    .orderBy(time to SortOrder.DESC)
                    .limit(1)
                    .map { retTime = it[time] }
        }
        return retTime
    }

    fun getLastState(name : String) : String = getStateAt(name, Long.MAX_VALUE)
}