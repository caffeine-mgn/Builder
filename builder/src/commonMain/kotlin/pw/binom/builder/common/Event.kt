package pw.binom.builder.common
/*
import pw.binom.builder.server.Status
import pw.binom.json.*


sealed class Event {

    class JobChangeState(val executeJob: ExecuteJob, val status: Status.Type) : Event() {
        override val type: String
            get() = "JobChangeState"

        override suspend fun write(ctx: ObjectCtx) {
            super.write(ctx)
            ctx.run {
                node("job") {
                    executeJob.write(this)
                }
                string("status", status.name)
            }
        }
    }

    class JobEntityCreated(val entity: JobEntity) : Event() {
        override val type: String
            get() = "JobEntityCreated"

        override suspend fun write(ctx: ObjectCtx) {
            super.write(ctx)
            ctx.run {
                node("job") {
                    entity.write(this)
                }
            }
        }
    }

    class JobEntityDeleted(val entity: JobEntity) : Event() {
        override val type: String
            get() = "JobEntityDeleted"

        override suspend fun write(ctx: ObjectCtx) {
            super.write(ctx)
            ctx.run {
                node("job") {
                    entity.write(this)
                }
            }
        }
    }

    class JobEntityModify(val entity: JobEntity) : Event() {
        override val type: String
            get() = "JobEntityModify"

        override suspend fun write(ctx: ObjectCtx) {
            super.write(ctx)
            ctx.run {
                node("job") {
                    entity.write(this)
                }
            }
        }
    }

    protected abstract val type: String
    open suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            string("type", type)
        }
    }

    companion object {
        suspend fun read(node: JsonNode) {
            val type = node.obj["type"]!!.text
            when (type) {
                "JobChangeState" -> JobChangeState(ExecuteJob.read(node.obj["job"]!!.obj), node.obj["status"]!!.text.let { Status.Type.valueOf(it) })
                "JobEntityCreated" -> JobEntityCreated(JobEntity.read(node.obj["job"]!!.obj))
                "JobEntityDeleted" -> JobEntityDeleted(JobEntity.read(node.obj["job"]!!.obj))
                "JobEntityModify" -> JobEntityModify(JobEntity.read(node.obj["job"]!!.obj))
            }
        }
    }
}

 */