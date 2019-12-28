package pw.binom.builder.remote

fun BuildDescription.toProcess() = JobProcess(
        buildNumber = buildNumber,
        path = path
)

enum class JobStatusType {
    PREPARE,
    PROCESS,
    FINISHED_OK,
    FINISHED_ERROR,
    CANCELED
}

val Event_TaskStatusChange.statusEnum
    get() = JobStatusType.valueOf(status)

val JobStatus.statusEnum
    get() = JobStatusType.valueOf(status)

fun JobStatus.newStatus(status: JobStatusType) =
        JobStatus(
                process = process,
                start = start,
                end = end,
                status = status.name
        )

val JobProcess.asShort
    get() = "$path:$buildNumber"