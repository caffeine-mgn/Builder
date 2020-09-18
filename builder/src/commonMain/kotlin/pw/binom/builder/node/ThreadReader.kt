package pw.binom.builder.node

import pw.binom.Input
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.Worker
import pw.binom.doFreeze
import pw.binom.io.EOFException
import pw.binom.io.Reader
import pw.binom.io.utf8Reader

//
//import pw.binom.AppendableQueue
//import pw.binom.Input
//import pw.binom.atomic.AtomicBoolean
//import pw.binom.builder.OutType
//import pw.binom.concurrency.Worker
//import pw.binom.doFreeze
//import pw.binom.io.EOFException
//import pw.binom.io.Reader
//import pw.binom.io.utf8Reader
//
//class Out(val value: String?, val type: OutType)
//
//class ThreadReader(inputStream: Input, val type: OutType, val out: AppendableQueue<Out>) : Thread() {
//    private val streamReader = inputStream.utf8Reader()
//    override fun run() {
//        while (!isInterrupted) {
//            try {
//                val l = streamReader.readln1()
//                out.push(Out(l, type))
//            } catch (e: EOFException) {
//                out.push(Out(null, type))
//                break
//            }
//        }
//    }
//}
//
class ThreadReader2(inputStream: Input, val out: (String?) -> Boolean) {
    private val streamReader = inputStream.utf8Reader()
    private var interrupted by AtomicBoolean(false)
    private val worker = Worker()

    init {
        doFreeze()
        worker.execute(this) {
            try {
                it.run()
            } finally {
                it.worker.requestTermination()
            }
        }
    }

    private fun run() {
        while (!interrupted) {
            try {
                val l = streamReader.readln1()
                if (!out(l))
                    break
            } catch (e: EOFException) {
                out(null)
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