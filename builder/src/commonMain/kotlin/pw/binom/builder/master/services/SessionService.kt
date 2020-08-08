package pw.binom.builder.master.services

import pw.binom.UUID
import pw.binom.date.Date
import pw.binom.db.Connection
import pw.binom.strong.Strong
import pw.binom.uuid
import kotlin.random.Random

class SessionService(strong: Strong) {
    val connection by strong.service<Connection>()

    fun createSession(userId: UUID, expirationDate: Date): UUID {
        val sessionId = Random.uuid()
        connection.prepareStatement("""insert into "sessions" (session, user, expirationDate) values(?, ?, ?)""").also {
            it.set(0, sessionId)
            it.set(1, userId)
            it.set(2, expirationDate.time)
            it.executeUpdate()
        }
        return sessionId
    }

    fun cleanup() {
        connection.prepareStatement("""delete from "sessions" s where s.expirationDate<?""").also {
            it.set(0, Date.now)
            it.executeUpdate()
        }
    }

    fun deleteSession(sessionId: UUID) {
        connection.prepareStatement("""delete from "sessions" s where s.session=?""").also {
            it.set(0, sessionId)
            it.executeUpdate()
        }
    }

    fun getSession(sessionId: UUID): UUID? =
            connection.prepareStatement("""select user from "sessions" s where s.expirationDate>=? and s.session=? limit 1""").let {
                it.set(0, Date.now)
                it.set(1, sessionId)
                it.executeQuery()
            }.map {
                it.getUUID(0)
            }.asSequence().singleOrNull()
}