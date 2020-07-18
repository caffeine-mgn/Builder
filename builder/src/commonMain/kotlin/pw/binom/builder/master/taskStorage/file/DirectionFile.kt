package pw.binom.builder.master.taskStorage.file

import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.file.File

class DirectionFile(override val file: File, override val taskStorage: TaskStorageFile) : AbstractEntityHolderFile(), TaskStorage.Direction