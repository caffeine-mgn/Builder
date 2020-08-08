package pw.binom.slash.dialect

import pw.binom.date.Date
import pw.binom.db.Connection
import pw.binom.io.use
import pw.binom.slash.Slash
import pw.binom.slash.SlashDialect

object SQLiteDialect : SlashDialect {

    override fun getAppliedBatchs(connection: Connection): List<String> {
        val out = ArrayList<String>()
        connection.createStatement().use {
            it.executeQuery("select id from `${Slash.MASTER_TABLE}` order by `date`").use {
                while (it.next()) {
                    out += it.getString(0)
                }
            }
        }
        return out
    }

    override fun prepare(connection: Connection) {
        connection.createStatement().use {
            it.executeUpdate("BEGIN TRANSACTION")
            try {
                it.executeUpdate("""create table if not exists "${Slash.MASTER_TABLE}"(
                "id" text PRIMARY KEY not null,
                "date" integer(8) not null
            )""")
                it.executeUpdate("COMMIT")
            } catch (e: Throwable) {
                it.executeUpdate("ROLLBACK")
                throw e
            }
        }
    }

    override fun apply(label: String, connection: Connection, func: (Connection) -> Unit) {
        connection.createStatement().use {
            it.executeUpdate("BEGIN TRANSACTION")
        }
        try {
            connection.prepareStatement("insert into \"${Slash.MASTER_TABLE}\" (\"id\",\"date\") values(?,?)").use {
                it.set(0, label)
                it.set(1, Date.now)
                it.executeUpdate()
            }
            func(connection)
            connection.createStatement().use {
                it.executeUpdate("COMMIT")
            }
        } catch (e: Throwable) {
            connection.createStatement().use {
                it.executeUpdate("ROLLBACK")
            }
            throw e
        }
    }

}