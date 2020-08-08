package pw.binom.builder

private const val MASK = "fc090188-685a-4b7d-97ba-6644e9c8238f"

object PasswordUtils {
    fun encode(char: String): String {
        val sb = StringBuilder()
        char.forEachIndexed { index, c ->
            sb.append((c.toInt() xor MASK[index % MASK.lastIndex].toInt()).toChar())
        }
        return sb.toString()
    }
}