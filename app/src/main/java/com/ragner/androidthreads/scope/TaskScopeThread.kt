package com.ragner.androidthreads.scope

class TaskScopeThread : TaskScopeKernelThread() {
    private var mIsRunning: Boolean = false

    override fun onStart() {
        if (!mIsRunning) {
            mIsRunning = true
        }
    }

    override fun onCancel() {
        mIsRunning = false
    }

    override fun onIsRunning(): Boolean = mIsRunning

    override fun onLaunch(maxTasks: Int) {
        repeat(maxTasks) { taskId ->
            Thread { onProcessTask(taskId) }.start()
        }
    }
}