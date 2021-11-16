package com.ragner.androidthreads.scope

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class TaskScopeThreadPool : TaskScopeKernelThread() {
    private val processors = Runtime.getRuntime().availableProcessors()
    private var executorService: ExecutorService? = null

    override fun onStart() {
        if (executorService == null) {
            executorService = ThreadPoolExecutor(processors, processors * 2,
                    100L, TimeUnit.MILLISECONDS, LinkedBlockingQueue<Runnable>()
                )
        }
    }

    override fun onCancel() {
        executorService?.shutdownNow()
        executorService = null
    }

    override fun onIsRunning(): Boolean = executorService != null

    override fun onLaunch(maxTasks: Int) {
        repeat(maxTasks) { taskId ->
            executorService?.execute { onProcessTask(taskId) }
        }
    }
}