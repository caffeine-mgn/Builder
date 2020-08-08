package pw.binom.builder.master

import pw.binom.Environment
import pw.binom.builder.master.services.UserService
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.strong.Strong
import pw.binom.workDirectory

class InstallService(strong: Strong) : Strong.InitializingBean {
    private val userService by strong.service<UserService>()
    private val installFile = File(File(Environment.workDirectory), "installed")
    fun isInstalled() = installFile.isFile

    fun install(login: String, password: String) {
        if (isInstalled()) {
            throw IllegalStateException("Builder already installed")
        }
        println("Try install")
        userService.addUser(login, password, "Administrator")
        installFile.write().close()
    }

    override fun init() {
        println("isInstalled()=${isInstalled()}")
        if (!isInstalled() && userService.getUserByLogin("admin") == null)
            install(
                    login = "admin",
                    password = "admin"
            )
    }
}