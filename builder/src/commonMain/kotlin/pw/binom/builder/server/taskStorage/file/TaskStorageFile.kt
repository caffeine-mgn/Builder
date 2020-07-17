package pw.binom.builder.server.taskStorage.file

import pw.binom.builder.server.taskStorage.TaskStorage
import pw.binom.io.file.File

class TaskStorageFile(override val path: String, override val file: File) : TaskStorage, AbstractEntityHolderFile() {
    override val taskStorage: TaskStorageFile
        get() = this
}