package com.ragner.androidthreads.scope

interface TaskScopeListener {
    fun onScopeStarted(maxTasks: Int)
    fun onScopeDone()

    fun onTaskStarted(taskId: Int, time: Long)
    fun onTaskWorking(taskId: Int, progress: Float)
    fun onTaskDone(taskId: Int, time: Long)
}