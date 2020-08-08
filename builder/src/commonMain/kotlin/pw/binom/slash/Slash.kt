package pw.binom.slash

import pw.binom.db.Connection
import pw.binom.io.use
import pw.binom.slash.dialect.SQLiteDialect

class Slash(val connection: Connection, val dialect: SQLiteDialect) {
    companion object {
        const val MASTER_TABLE = "slash_master"
    }

    init {
        dialect.prepare(connection)
    }

    private val batchs = dialect.getAppliedBatchs(connection)

    @OptIn(ExperimentalStdlibApi::class)
    fun apply(label: String, sql: String) {
        if (label in batchs) {
            return
        }
        dialect.apply(label, connection) {
            it.createStatement().use {
                it.executeUpdate(sql)
            }
        }
    }
}