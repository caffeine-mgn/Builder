package pw.binom.builder.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class User(val login: String, val name: String)