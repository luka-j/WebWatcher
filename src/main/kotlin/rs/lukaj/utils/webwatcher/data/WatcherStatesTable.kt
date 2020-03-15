package rs.lukaj.utils.webwatcher.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import rs.lukaj.utils.webwatcher.WatcherState

object WatcherStatesTable : Table() {
    val name = text("name")
    val currentText = text("currentText")
    val currentHTML = text("currentHTML")
    val lastInvocation = long("lastInvocation")

    init {
        transaction {
            SchemaUtils.create(WatcherStatesTable)
        }
    }

    override val primaryKey = PrimaryKey(name, name="PKConstraint")

    private fun insertWatcher(name : String) {
        WatcherStatesTable.insert {
            it[this.name] = name
            it[this.currentHTML] = ""
            it[this.currentText] = ""
            it[this.lastInvocation] = 0
        }
    }

    fun setCurrentState(watcherName : String, text : String, html: String) {
        transaction {
            WatcherStatesTable.update({name eq watcherName}) {
                it[currentText] = text
                it[currentHTML] = html
            }

            val lastState = WatcherHistoryTable.getLastState(watcherName)
            if(lastState != text) {
                WatcherHistoryTable.insert {
                    it[WatcherHistoryTable.watcherName] = watcherName
                    it[time] = System.currentTimeMillis()
                    it[WatcherHistoryTable.text] = text
                    it[WatcherHistoryTable.html] = html
                }
            }
        }
    }

    fun getState(watcherName: String) : WatcherState {
        val state = WatcherState()
        transaction {
            val count = WatcherStatesTable.select { name eq watcherName }.map {
                state.current = it[currentText]
                state.currentHTML = it[currentHTML]
                state.lastInvocation = it[lastInvocation]
            }.count()
            if(count == 0) insertWatcher(watcherName)
        }
        return state
    }
}

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