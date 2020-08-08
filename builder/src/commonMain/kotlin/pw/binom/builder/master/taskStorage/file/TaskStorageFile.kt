package pw.binom.builder.master.taskStorage.file

import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.file.File

class TaskStorageFile(override val file: File) : TaskStorage, AbstractEntityHolderFile() {
    override val path: String
        get() = ""
    override val taskStorage: TaskStorageFile
        get() = this

    override fun delete() {
        throw IllegalStateException("Can't delete root task storage")
    }
}