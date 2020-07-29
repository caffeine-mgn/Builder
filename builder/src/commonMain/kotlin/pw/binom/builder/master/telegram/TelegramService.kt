package pw.binom.builder.master.telegram

import pw.binom.async
import pw.binom.builder.common.Action
import pw.binom.builder.master.MasterThread
import pw.binom.builder.master.SlaveService
import pw.binom.builder.master.TaskScheduler
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.file.write
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.readText
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.printStacktrace
import pw.binom.strong.Strong
import pw.binom.telegram.TelegramApi
import pw.binom.thread.Thread

class TelegramService(strong: Strong, val basePath: File, val token: String) : Strong.InitializingBean {
    private val recive = SocketNIOManager()
    private val telegramFile = File(basePath, "telegram.ini")
    private val slaveService by strong.service<SlaveService>()
    private val taskScheduler by strong.service<TaskScheduler>()
    private val reciveApi = run {
        val lastMessage = if (telegramFile.isFile)
            telegramFile.read().utf8Reader().use { it.readText() }.toLong()
        else
            0L
        TelegramApi(lastMessage, token, recive)
    }

    override fun init() {
        reciveThread.start()
    }

    private fun processingMessage(chatId: Long, cmd: String) {
        if (cmd.startsWith("build ")) {
            val task = cmd.removePrefix("build ")
            taskScheduler.submitTask(task)
        }
        if (cmd == "keys") {
            val txt = masterThread.manager.keys.mapIndexed { index, selectorKey ->
                "`$index` -> `${selectorKey.listenReadable}`/`${selectorKey.listenWritable}`"
            }.joinToString("\n")
            async {
                try {
                    reciveApi.sendMessage(TelegramApi.TextMessage(
                            chat_id = chatId.toString(),
                            text = "**Keys**:\n$txt"
//                        parseMode = TelegramApi.ParseMode.MARKDOWN_V2
                    ))
                } catch (e: Throwable) {
                    e.printStacktrace()
                }
            }
        }

        if (cmd == "test") {
            async {
                println("Send Ping!")
                slaveService.slaves.values.forEach {
                    it.execute(Action.Ping())
                }
            }
        }
    }

    private val masterThread by strong.service<MasterThread>()


    private val reciveThread = object : Thread() {
        override fun run() {
            async {
                try {
                    while (true) {
                        val updates = reciveApi.getUpdate()
                        updates.forEach {
                            val message = it.message ?: return@forEach
//                            println("${message.from?.userName?.let { "@$it: " }}${message.text}")
                            if (message.text != null) {
                                processingMessage(message.chat.id, message.text)
//                                reciveApi.sendMessage(TelegramApi.TextMessage(
//                                        chat_id = message.chat.id.toString(),
//                                        text = "Echo ${message.text}"
//                                ))
                            }
                        }
                        telegramFile.write().utf8Appendable().use {
                            it.append(reciveApi.lastUpdate.toString())
                        }
                    }
                } catch (e: Throwable) {
                    e.printStacktrace()
                }
            }
            while (!isInterrupted) {
                recive.update()
            }
        }
    }
}