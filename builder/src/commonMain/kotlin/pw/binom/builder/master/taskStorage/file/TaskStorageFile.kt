package pw.binom.builder.master.taskStorage.file

import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.file.File

class TaskStorageFile(override val path: String, override val file: File) : TaskStorage, AbstractEntityHolderFile() {
    override val taskStorage: TaskStorageFile
        get() = this
}