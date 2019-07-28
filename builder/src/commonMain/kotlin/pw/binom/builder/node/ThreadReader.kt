package pw.binom.builder.node

import pw.binom.AppendableQueue
import pw.binom.builder.OutType
import pw.binom.io.EOFException
import pw.binom.io.InputStream
import pw.binom.io.Reader
import pw.binom.io.utf8Reader
import pw.binom.job.Task

class Out(val value: String?, val type: OutType)

class ThreadReader(inputStream: InputStream, val type: OutType, val out: AppendableQueue<Out>) : Task() {
    private val streamReader = inputStream.utf8Reader()
    override fun execute() {
        while (!isInterrupted) {
            try {
                val l = streamReader.readln1()
                out.push(Out(l, type))
            } catch (e: EOFException) {
                out.push(Out(null, type))
                break
            }
        }
    }
}

fun Reader.readln1(): String {
    val sb = StringBuilder()
    var first = true
    while (true) {
        val r = read()
        if (r == null && first)
            throw EOFException()
        first = false
        if (r == null)
            break

        if (r == 10.toChar())
            break
        if (r == 13.toChar())
            continue
        sb.append(r)
    }
    return sb.toString()
}