package pw.binom.builder.master.entities

import pw.binom.UUID
import pw.binom.builder.dto.User

class DBUser(val id: UUID, val login: String, val name: String, val password: ByteArray) {
    fun toDTO() = User(
            login = login,
            name = name
    )
}