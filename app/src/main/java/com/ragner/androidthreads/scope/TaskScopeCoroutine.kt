package com.ragner.androidthreads.scope

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.*

class TaskScopeCoroutine(private val dispatcher: CoroutineDispatcher) : TaskScope() {
    private var scope: CoroutineScope? = null

    override fun onStart() {
        if (scope == null) {
            scope = CoroutineScope(Job() + dispatcher)
        }
    }

    override fun onCancel() {
        scope?.cancel()
        scope = null
    }

    override fun onIsRunning(): Boolean = scope != null

    override fun onLaunch(maxTasks: Int) {
        repeat(maxTasks) { taskId ->
            scope?.launch { onProcessTask(taskId) }
        }
    }

    private suspend fun onProcessTask(taskId: Int) {
        val threadId = Thread.currentThread().id
        var scopeTimeElapsed = SystemClock.elapsedRealtime() - mScopeStartTime

        onTaskStarted(taskId, scopeTimeElapsed)

        Log.d(TAG, "[${scopeTimeElapsed/1000f}s] - Thread[$threadId], Task[$taskId] > working...")

        onSimulateHeavyTask(taskId)

        scopeTimeElapsed = SystemClock.elapsedRealtime() - mScopeStartTime
        Log.d(TAG, "[${scopeTimeElapsed/1000f}s] - Thread[$threadId], Task[$taskId] > done!!!")

        onTaskDone (taskId, scopeTimeElapsed)
    }

    private suspend fun onSimulateHeavyTask(taskId: Int) : Boolean {
        repeat(mMaxLoop) {
            if (!isRunning()) { return false }

            if (it % mSleepEachCount == 0) {
                scopeLister?.onTaskWorking(taskId, it / mMaxLoop.toFloat())

                if (sleepEnabled) delay(mSleepDelay)
            }
        }

        return true
    }
}