package pw.binom.builder.master

import pw.binom.db.Connection
import pw.binom.db.sqlite.SQLiteConnector
import pw.binom.io.file.File
import pw.binom.slash.Slash
import pw.binom.slash.dialect.SQLiteDialect
import pw.binom.strong.Strong

fun dataBaseConfig(fileDir: File) = Strong.config { strong ->
    strong.define(SQLiteConnector.openFile(File(fileDir, "database.db")))
    strong.define(DatabaseUpdater(strong))
}

private class DatabaseUpdater(strong: Strong) : Strong.InitializingBean {
    val connection by strong.service<Connection>()
    override fun init() {
        val slash = Slash(
                connection,
                when (connection.type) {
                    SQLiteConnector.TYPE -> SQLiteDialect
                    else -> throw RuntimeException("Unknown database ${connection.type}")
                }
        )

        slash.apply("subochev",
                """
                   create table "users" (
                        "id" blob(16) PRIMARY KEY,
                        "login" TEXT not null,
                        "password" blob(20) not null,
                        "name" text not null
                   );
                   
                   create table "sessions" (
                        "session" blob(16) PRIMARY KEY,
                        "user" blob(16) not null,
                        "expirationDate" integer(8) not null
                   )
                """
        )
    }

}