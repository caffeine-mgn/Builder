package pw.binom.builder.master.taskStorage.file

import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.file.File
import pw.binom.io.file.deleteRecursive

class DirectionFile(override val file: File, override val taskStorage: TaskStorageFile) : AbstractEntityHolderFile(), TaskStorage.Direction{
    override fun delete() {
        file.deleteRecursive()
    }

}