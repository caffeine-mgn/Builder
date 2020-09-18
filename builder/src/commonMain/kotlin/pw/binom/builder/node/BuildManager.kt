package pw.binom.builder.node

import pw.binom.io.file.File
import pw.binom.io.file.mkdirs
import pw.binom.strong.Strong
import pw.binom.uuid
import kotlin.random.Random

class BuildManager(strong: Strong) {
    val config by strong.service<NodeConfig>()
    fun prepareBuildDir() = File(config.baseDir, Random.uuid().toShortString()).apply { mkdirs() }
}