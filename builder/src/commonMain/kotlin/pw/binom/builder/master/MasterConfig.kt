package pw.binom.builder.master

import pw.binom.ByteBufferPool
import pw.binom.builder.master.controllers.*
import pw.binom.builder.master.services.SessionService
import pw.binom.builder.master.services.TaskSchedulerService
import pw.binom.builder.master.services.UserService
import pw.binom.builder.master.taskStorage.file.TaskStorageFile
import pw.binom.builder.master.telegram.TelegramDatabaseService
import pw.binom.builder.master.telegram.TelegramService
import pw.binom.flux.RootRouter
import pw.binom.io.file.File
import pw.binom.strong.EventSystem
import pw.binom.strong.Strong

const val POOL_3MB = "pool3mb"
const val POOL_8KB = "pool8kb"

fun masterConfig(bind: List<Pair<String, Int>>, telegramToken: String?, tasksRoot: File) = Strong.config {
    it.define(MasterThread(it, bind))
    it.define(RootRouter())
    it.define(ByteBufferPool(30, 1024u * 1024u * 3u), POOL_3MB)
    it.define(ByteBufferPool(30, 1024u * 8u), POOL_8KB)
    it.define(EventSystem())
    it.define(SlaveService(it))
    it.define(InstallService(it))
    it.define(SlaveController(it))
    it.define(UserController(it))
    it.define(SessionService(it))
    it.define(UserService(it))
    it.define(TaskSchedulerService(it))
    it.define(EventBusHandler(it))
    it.define(ActionExecutor(it))
    it.define(UIController(it))
    it.define(TasksController(it))
    it.define(TaskSchedulerController(it))
    it.define(WorkersController(it))
    it.define(TaskStorageFile(tasksRoot))
    if (telegramToken != null) {
        telegramConfig(
                telegramToken = telegramToken,
                tasksRoot = tasksRoot
        ).apply(it)
    }

    dataBaseConfig(tasksRoot).apply(it)
}

fun telegramConfig(telegramToken: String, tasksRoot: File) = Strong.config {
    it.define(TelegramService(it, tasksRoot, telegramToken))
    it.define(TelegramDatabaseService(File(tasksRoot, "telegram.db"), it))
}