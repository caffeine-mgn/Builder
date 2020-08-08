package pw.binom.builder.master.services

import pw.binom.UUID
import pw.binom.builder.master.entities.DBUser
import pw.binom.db.Connection
import pw.binom.io.Sha1
import pw.binom.strong.Strong
import pw.binom.uuid
import kotlin.random.Random

@OptIn(ExperimentalStdlibApi::class)
private fun paswordHash(password: String): ByteArray {
    val sha1 = Sha1()
    password.encodeToByteArray().forEach {
        sha1.update(it)
    }
    return sha1.finish()
}

class UserService(strong: Strong) {
    private val connection by strong.service<Connection>()

    fun addUser(login: String, password: String, name: String): DBUser {
        val id = Random.uuid()
        val d = DBUser(
                id = id,
                name = name,
                password = paswordHash(password),
                login = login
        )
        connection.prepareStatement("""insert into "users"(id,login,password,name) values(?,?,?,?)""").apply {
            set(0, id)
            set(1, login)
            set(2, d.password)
            set(3, name)
        }.executeUpdate()
        return d
    }

    fun getUserByLogin(login: String): DBUser? {
        return connection.prepareStatement("""select u.id, u.login, u.password, u.name from "users" u where u.login=? limit 1""").let {
            it.set(0, login)
            it.executeQuery()
        }.map {
            DBUser(
                    id = it.getUUID(0),
                    login = it.getString(1),
                    password = it.getBlob(2),
                    name = it.getString(3)
            )
        }.asSequence().singleOrNull()
    }

    fun getUserById(userId: UUID): DBUser? {
        return connection.prepareStatement("""select u.id, u.login, u.password, u.name from "users" u where u.id=? limit 1""").let {
            it.set(0, userId)
            it.executeQuery()
        }.map {
            DBUser(
                    id = it.getUUID(0),
                    login = it.getString(1),
                    password = it.getBlob(2),
                    name = it.getString(3)
            )
        }.asSequence().singleOrNull()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun checkLoginPassword(login: String, password: String): DBUser? {
        return connection.prepareStatement("""select u.id, u.login, u.password, u.name from "users" u where u.login=? and password=? limit 1""").let {
            it.set(0, login)
            it.set(1, paswordHash(password))
            it.executeQuery()
        }.map {
            DBUser(
                    id = it.getUUID(0),
                    login = it.getString(1),
                    password = it.getBlob(2),
                    name = it.getString(3)
            )
        }.asSequence().singleOrNull()
    }
}