package pw.binom.builder.node

import pw.binom.builder.common.Action
import pw.binom.io.Writer
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.io.utf8Appendable
import pw.binom.strong.Strong
import pw.binom.uuid
import kotlin.random.Random

class LogOutput(strong: Strong, val baseDir: File) {

    //    private var subscribe = false
    private val client by strong.service<ClientThread>()
//    private var currentLogFile: File? = null
//    private var appender: Writer? = null

//    fun prepareNewBuild() {
//        val file = File(baseDir, Random.uuid().toShortString())
//        appender?.close()
//        currentLogFile?.delete()
//        currentLogFile = file
//        appender = file.write().utf8Appendable()
//    }

    fun stdout(text: String) {
        println("std: $text")
//        if (subscribe) {
        client.send(Action.LogRecord(text, true))
//        }
//        appender?.append("STDOUT:")?.append(text)?.append('\n')
    }

    fun errout(text: String) {
        println("err: $text")
//        if (subscribe) {
        client.send(Action.LogRecord(text, false))
//        }
//        appender?.append("ERROUT:")?.append(text)?.append('\n')
    }

//    fun unsubscribe() {
//        subscribe = false
//    }

//    fun subscribe() {
//        subscribe = true
//    }
}