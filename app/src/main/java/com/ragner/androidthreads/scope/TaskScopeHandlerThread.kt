package com.ragner.androidthreads.scope

import android.os.Handler
import android.os.HandlerThread
import android.os.Process

class TaskScopeHandlerThread : TaskScopeKernelThread() {
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    override fun onStart() {
        if (handlerThread == null) {
            handlerThread = HandlerThread("HandlerThread",
                Process.THREAD_PRIORITY_DEFAULT)
            handlerThread?.also {
                it.start()
                handler = Handler(it.looper)
            }
        }
    }

    override fun onCancel() {
        handlerThread?.quit()

        handlerThread = null
        handler = null
    }

    override fun onIsRunning(): Boolean = handler != null

    override fun onLaunch(maxTasks: Int) {
        repeat(maxTasks) { taskId ->
            handler?.post { onProcessTask(taskId) }
        }
    }
}