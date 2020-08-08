package pw.binom.slash

import pw.binom.db.Connection

interface SlashDialect {
    fun getAppliedBatchs(connection: Connection): List<String>
    fun prepare(connection: Connection)
    fun apply(label: String, connection: Connection, func: (Connection) -> Unit)
}