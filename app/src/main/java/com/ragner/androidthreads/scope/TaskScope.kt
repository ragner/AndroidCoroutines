package com.ragner.androidthreads.scope

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicInteger

abstract class TaskScope {
    protected val TAG = this.javaClass.simpleName
    protected var mScopeStartTime = 0L
    protected var mMaxTasks = 0
    private var mCountTasksDone: AtomicInteger = AtomicInteger(0)

    var scopeLister: TaskScopeListener? = null
    var sleepEnabled = true

    fun start() {
        if (!isRunning()) {
            mScopeStartTime = SystemClock.elapsedRealtime()
            mCountTasksDone.set(0)

            onStart()
        }
    }

    fun cancel() {
        if (isRunning()) {
            onCancel()
        }

        scopeLister?.onScopeDone()
    }

    fun launch(maxTasks: Int) {
        if (isRunning()) {
            mMaxTasks = maxTasks

            scopeLister?.onScopeStarted(maxTasks)

            onLaunch(maxTasks)
        }
    }

    protected fun isRunning() : Boolean {
        return onIsRunning()
    }

    protected fun onTaskStarted(taskId: Int, time: Long) {
        scopeLister?.onTaskStarted(taskId, time)
    }

    protected fun onTaskDone(taskId: Int, time: Long) {
        scopeLister?.onTaskDone(taskId, time)

        if (mCountTasksDone.addAndGet(1) == mMaxTasks && isRunning()) {
            scopeLister?.onScopeDone()
        }
    }

    protected abstract fun onStart()
    protected abstract fun onCancel()
    protected abstract fun onIsRunning() : Boolean
    protected abstract fun onLaunch(maxTasks: Int)

    companion object {
        const val mMaxLoop = 0xf000000
        const val mSleepEachCount = 0xf0000
        const val mSleepDelay = 1L
    }
}