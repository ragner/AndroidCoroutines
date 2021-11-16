package com.ragner.androidthreads.scope

import android.os.SystemClock
import android.util.Log

abstract class TaskScopeKernelThread : TaskScope() {

    protected fun onProcessTask(taskId: Int) {
        val threadId = Thread.currentThread().id
        var scopeTimeElapsed = SystemClock.elapsedRealtime() - mScopeStartTime

        onTaskStarted(taskId, scopeTimeElapsed)

        Log.d(TAG, "[${scopeTimeElapsed/1000f}s] - Thread[$threadId], Task[$taskId] > working...")

        onSimulateHeavyTask(taskId)

        scopeTimeElapsed = SystemClock.elapsedRealtime() - mScopeStartTime
        Log.d(TAG, "[${scopeTimeElapsed/1000f}s] - Thread[$threadId], Task[$taskId] > done!!!")

        onTaskDone (taskId, scopeTimeElapsed)
    }

    private fun onSimulateHeavyTask(taskId: Int) : Boolean {
        repeat(mMaxLoop) {
            if (!isRunning()) { return false }

            if (it % mSleepEachCount == 0) {
                scopeLister?.onTaskWorking(taskId, it / mMaxLoop.toFloat())

                try {
                    if (sleepEnabled) Thread.sleep(mSleepDelay)
                } catch (e: InterruptedException) {
                    return false
                }
            }
        }

        return true
    }
}