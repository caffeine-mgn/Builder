package pw.binom.builder.node

import pw.binom.URL
import pw.binom.io.file.File
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.strong.Strong

fun slaveConfig(config:NodeConfig) = Strong.config {
    it.define(config)
    it.define(SocketNIOManager())
    it.define(Client2(strong = it))

//    it.define(LogOutput(
//            strong = it
//    ))

    it.define(BuildManager(it))
}