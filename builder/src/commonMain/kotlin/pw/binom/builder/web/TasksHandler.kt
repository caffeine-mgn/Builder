package pw.binom.builder.web

/*
class TasksHandler(
        val taskManager: TaskManager,
        val executeScheduler: ExecuteScheduler,
        val executionControl: ExecutionControl
) : PathHandler() {
    private val LOG = Logger.getLog("/tasks")

    init {
        filter(method("GET")) { r, q ->


            val job2 = taskManager.getJob(r.contextUriWithoutParams)

            when (r.params["action"]) {
                "builds" -> {
                    q.status = 200
                    q.output.utf8Appendable().append(getBuilds(job2 ?: r.pageNotFound()))
                    return@filter
                }
                "tailOut" -> tailEnd(job2 ?: r.pageNotFound(), r, q)
                "tailOut" -> tailStart(job2 ?: r.pageNotFound(), r, q)
                null -> {
                    val list = taskManager.getPath(r.contextUri.decodeUrl())
                            ?.map {
                                when (it) {
                                    is TaskManager.Job -> JobEntity.Job(it.path)
                                    is TaskManager.Dir -> JobEntity.Folder(it.path)
                                    else -> TODO()
                                }
                            }

                    if (list != null) {
                        q.status = 200
                        q.resetHeader("Content-Type", "application/json")
                        val writer = q.output.utf8Appendable()
                        jsonArray(writer) {
                            list.forEach {
                                node {
                                    it.write(this)
                                }
                            }
                        }
                        q.output.flush()
                        return@filter
                    }
                    job2 ?: r.pageNotFound()

                    q.status = 200
                    jsonNode(q.output.utf8Appendable()) {
                        job2.jobFile().toJobInformation().write(this)
                    }
                    q.output.flush()
                    return@filter
                }
                else -> r.pageNotFound()
            }


        }

        filter(method("POST")) { r, q ->
            val job = { taskManager.getJob(r.contextUriWithoutParams.decodeUrl()) }
            val dir = { taskManager.getDir(r.contextUriWithoutParams.decodeUrl()) }
            q.status = 200
            val action = r.params["action"]

            when (action) {
                "editJob" -> editJob(
                        job = job() ?: r.pageNotFound(),
                        jobInformation = JobInformation.read(r.input.utf8Reader().readJson()
                        )
                )
                "createDir" -> createDir(
                        dir = dir() ?: r.pageNotFound(),
                        name = r.param("name")
                )
                "createJob" -> q.output.utf8Appendable().append(createJob(
                        dir = dir() ?: r.pageNotFound(),
                        name = r.param("name"),
                        jobInformation = JobInformation.read(r.input.utf8Reader().readJson())
                ).write())
                "execute" -> q.output.utf8Appendable().append(executeJob(job() ?: r.pageNotFound()))
            }
        }
/*
        filter(method("POST") + endsWith("/createDir")) { r, q ->
            val dir = taskManager.getDir(r.contextUriWithoutParams.decodeUrl())
            if (dir == null) {
                LOG.warn("Job ${r.contextUri.decodeUrl()} not found")
                q.status = 404
                return@filter
            }
            val name = r.params["name"]
            if (name == null) {
                q.status = 400
                return@filter
            }
            q.status = 200
            jsonNode(q.output.utf8Appendable()) {
                JobEntity.Folder(dir.createDir(name).path).write(this)
            }
            LOG.info("Create job ${r.contextUriWithoutParams.decodeUrl()}")
        }

        filter(method("POST") + endsWith("/createJob")) { r, q ->
            val dir = taskManager.getDir(r.contextUriWithoutParams.decodeUrl())
            if (dir == null) {
                LOG.warn("Job ${r.contextUri.decodeUrl()} not found")
                q.status = 404
                return@filter
            }
            val name = r.params["name"]
            if (name == null) {
                q.status = 400
                return@filter
            }
            q.status = 200
            val jobInformation = JobInformation.read(r.input.utf8Reader().readJson())
            jsonNode(q.output.utf8Appendable()) {
                JobEntity.Job(dir.createJob(name, jobInformation).path).write(this)
            }
            LOG.info("Create job ${r.contextUriWithoutParams.decodeUrl()}")
        }

        filter(method("POST") + endsWith("/execute")) { req, resp ->
            val job = taskManager.getJob(req.contextUri.decodeUrl())
            if (job == null) {
                LOG.info("Job ${req.contextUri.decodeUrl()} not found")
                resp.status = 404
                return@filter
            }

            resp.status = 200
            executeScheduler.execute(job).write(resp.output.utf8Appendable())
            resp.output.flush()
        }

        filter(method("GET") + endsWith("/outputfirst")) { req, resp ->
            val path = req.contextUriWithoutParams.decodeUrl().removePrefix("/")
            val p = path.lastIndexOf('/')

            val job = taskManager.getJob(path.substring(0, p))
            val buildNumber = path.substring(p + 1).toLong()

            if (job == null) {
                LOG.info("Job ${req.contextUri.decodeUrl()} not found")
                resp.status = 404
                return@filter
            }
            val size = req.params["size"]?.toLongOrNull()
            if (size == null) {
                resp.status = 400
                return@filter
            }

            resp.status = 200
            LOG.info("Getting output of job ${job.path}:$buildNumber")
            val out = job.getOutputFirst(buildNumber, size) ?: return@filter
            val app = resp.output.utf8Appendable()
            out.use {
                try {
                    while (true) {
                        app.append(it.readln()).append("\n")
                    }
                } catch (e: EOFException) {
                    //NOP
                }
            }
            resp.output.flush()
        }

        filter(method("GET") + endsWith("/outputlast")) { req, resp ->
            val path = req.contextUriWithoutParams.decodeUrl().removePrefix("/")
            val p = path.lastIndexOf('/')

            val job = taskManager.getJob(path.substring(0, p))
            val buildNumber = path.substring(p + 1).toLong()

            if (job == null) {
                LOG.info("Job ${req.contextUri.decodeUrl()} not found")
                resp.status = 404
                return@filter
            }
            resp.status = 200
            val size = req.params["size"]?.toIntOrNull() ?: 1024 * 1024
            LOG.info("Getting output of job ${job.path}:$buildNumber")
            val out = job.getOutputLast(buildNumber, size) ?: LastOutDto(text = "", skipped = 0)
            jsonNode(resp.output.utf8Appendable()) {
                out.write(this)
            }
            resp.output.flush()
        }
*/
        /*
        filter(method("GET") + endsWith("/builds")) { req, resp ->
            val job = taskManager.getJob(req.contextUri.decodeUrl().removePrefix("/"))
            if (job == null) {
                LOG.info("Job ${req.contextUri.decodeUrl()} not found")
                resp.status = 404
                return@filter
            }
            resp.status = 200
            jsonArray(resp.output.utf8Appendable()) {
                job.getBuilds().forEach {
                    node {
                        it.write(this)
                    }
                }
            }
            resp.output.flush()
        }
        */
        filter(method("GET") + equal("/wait")) { req, resp ->
            val nodeInfo = NodeInfo.read(req.input.utf8Reader())
            executionControl.idleNode(nodeInfo)

            val jobDescription = executeScheduler.getExecute(nodeInfo.platform, 60_000 * 10)
            if (jobDescription == null) {
                resp.status = 408
                executionControl.reset(nodeInfo)
                return@filter
            }
            try {
                resp.status = 200
                jobDescription.write(resp.output.utf8Appendable())
                resp.output.flush()
                LOG.info("Start job: ${jobDescription.path}:${jobDescription.buildNumber} on ${nodeInfo.toInfo()}")
                executionControl.execute(nodeInfo, jobDescription.toExecuteJob())
            } catch (e: IOException) {
                LOG.info("Node ${nodeInfo.toInfo()} is broken")
                executionControl.reset(nodeInfo)
                executeScheduler.execute(taskManager.getJob(jobDescription.path)!!)
            }
        }
    }

    private suspend fun executeJob(job: TaskManager.Job) = executeScheduler.execute(job).write()

    private suspend fun createDir(dir: TaskManager.Dir, name: String): JobEntity.Folder =
            JobEntity.Folder(dir.createDir(name).path)

    private suspend fun editJob(job: TaskManager.Job, jobInformation: JobInformation) {
        job.jobFile().save(jobInformation)
    }

    private suspend fun createJob(dir: TaskManager.Dir, name: String, jobInformation: JobInformation): JobEntity.Job =
            JobEntity.Job(dir.createJob(name, jobInformation).path)

    suspend fun getBuilds(job: TaskManager.Job): JsonArray =
            jsonArrayOf(job.getBuilds().map { it.write() })

    private suspend fun tailStart(job: TaskManager.Job, req: HttpRequest, resp: HttpResponse){
        val path = req.contextUriWithoutParams.decodeUrl().removePrefix("/")
        val p = path.lastIndexOf('/')

        val buildNumber = req.param("build").toLong()
        val size = req.param("size").toLong()


        resp.status = 200
        LOG.info("Getting output of job ${job.path}:$buildNumber")
        val out = job.getOutputFirst(buildNumber, size) ?: return
        val app = resp.output.utf8Appendable()
        out.use {
            try {
                while (true) {
                    app.append(it.readln()).append("\n")
                }
            } catch (e: EOFException) {
                //NOP
            }
        }
        resp.output.flush()
    }

    private suspend fun tailEnd(job: TaskManager.Job, req: HttpRequest, resp: HttpResponse) {
        val buildNumber = req.param("build").toLong()
        val size = req.param("size").toLong()

        resp.status = 200
        LOG.info("Getting output of job ${job.path}:$buildNumber")
        val out = job.getOutputFirst(buildNumber, size) ?: return
        val app = resp.output.utf8Appendable()
        out.use {
            try {
                while (true) {
                    app.append(it.readln()).append("\n")
                }
            } catch (e: EOFException) {
                //NOP
            }
        }
        resp.output.flush()
    }
}
*/