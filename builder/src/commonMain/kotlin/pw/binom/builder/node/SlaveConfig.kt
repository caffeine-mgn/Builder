package pw.binom.builder.node

import pw.binom.URL
import pw.binom.io.file.File
import pw.binom.strong.Strong

fun slaveConfig(bashPath: File, baseDir: File, tags: Set<String>, name: String, serverUrL: URL) = Strong.config {
    it.define(ClientThread(
            strong = it,
            bashPath = bashPath,
            tags = tags,
            name = name,
            serverUrl = serverUrL
    ))

    it.define(LogOutput(
            strong = it,
            baseDir = baseDir
    ))

    it.define(BuildManager(it, baseDir))
}