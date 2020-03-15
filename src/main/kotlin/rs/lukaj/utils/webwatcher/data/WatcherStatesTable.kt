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
                it[lastInvocation] = System.currentTimeMillis()
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

