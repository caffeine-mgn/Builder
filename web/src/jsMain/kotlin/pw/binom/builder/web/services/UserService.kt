package pw.binom.builder.web.services

import kotlinx.serialization.json.Json
import org.w3c.xhr.FormData
import pw.binom.builder.PasswordUtils
import pw.binom.builder.dto.User
import pw.binom.builder.web.Request1
import pw.binom.builder.web.UnauthorizedException

object UserService {
    suspend fun login(login: String, password: String): User? =
            try {
                val form = FormData()
                form.append("login", login)
                form.append("password", PasswordUtils.encode(password))
                val output = Request1.post("/api/login", form)
                Json.parse(User.serializer(), output)
            } catch (e: UnauthorizedException) {
                null
            }

    suspend fun whoIAm() =
            try {
                Json.parse(User.serializer(), Request1.get("/api/whoiam"))
            } catch (e: UnauthorizedException) {
                null
            }

}