package pw.binom.builder.node

import pw.binom.URL
import pw.binom.io.file.File

data class NodeConfig(
        val serverUrl: URL,
        val name: String,
        val tags: Set<String>,
        val bashPath: File,
        val baseDir: File
)