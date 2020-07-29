package pw.binom.builder.master

import pw.binom.builder.master.taskStorage.file.TaskStorageFile
import pw.binom.builder.master.telegram.TelegramDatabaseService
import pw.binom.builder.master.telegram.TelegramService
import pw.binom.flux.RootRouter
import pw.binom.io.file.File
import pw.binom.strong.EventSystem
import pw.binom.strong.Strong

//const val EVENT_TOPIC = "eventTopic"
//const val SLAVE_FREE_TOPIC = "slaveFreeTopic"

fun masterConfig(bind: List<Pair<String, Int>>, telegramToken: String?, tasksRoot: File) = Strong.config {
    it.define(MasterThread(it, bind))
    it.define(MasterRootController(it))
    it.define(RootRouter())
//    it.define(Topic<Event>(), EVENT_TOPIC)
//    it.define(Topic<SlaveFreeEvent>(), SLAVE_FREE_TOPIC)
    it.define(EventSystem())
    it.define(SlaveService(it))
    it.define(SlaveHandler(it))
    it.define(TaskScheduler(it))
    it.define(EventBusHandler(it))
    it.define(ActionExecutor(it))
    it.define(TaskStorageFile("", tasksRoot))
    if (telegramToken != null) {
        telegramConfig(
                telegramToken = telegramToken,
                tasksRoot = tasksRoot
        ).apply(it)
    }
}

fun telegramConfig(telegramToken: String, tasksRoot: File) = Strong.config {
    it.define(TelegramService(it, tasksRoot, telegramToken))
    it.define(TelegramDatabaseService(File(tasksRoot, "telegram.db"), it))
}