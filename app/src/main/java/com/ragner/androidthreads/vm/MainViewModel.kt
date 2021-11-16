package com.ragner.androidthreads.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ragner.androidthreads.scope.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext

class MainViewModel : ViewModel(), TaskScopeListener {
    var scopeLister: TaskScopeListener? = null
    var sleepEnabled = true

    private var mTaskScope: TaskScope? = null

    fun doStart(maxTasks: Int, threadType: Int) {
        Log.d(TAG, "Starting $maxTasks threads...")

        mTaskScope = newTaskScope(threadType)
        mTaskScope?.also { scope ->
            scope.scopeLister = this
            scope.sleepEnabled = sleepEnabled
            scope.start()
            scope.launch(maxTasks)
        }
    }

    fun doCancel() {
        mTaskScope?.cancel()
        mTaskScope = null
    }


    companion object {
        private val TAG = this::class.simpleName

        private fun newTaskScope(threadType: Int): TaskScope? {
            when (threadType) {
                0 -> return TaskScopeHandlerThread()
                1 -> return TaskScopeThread()
                2 -> return TaskScopeThreadPool()
                3 -> return TaskScopeCoroutine(Dispatchers.Default)
                4 -> return TaskScopeCoroutine(Dispatchers.IO)
                5 -> return TaskScopeCoroutine(newFixedThreadPoolContext(4, "MyOwnThread"))
                else -> Log.w(TAG, "Failed!!!")
            }

            return null
        }
    }

    override fun onScopeStarted(maxTasks: Int) {
        scopeLister?.onScopeStarted(maxTasks)
    }

    override fun onScopeDone() {
        scopeLister?.onScopeDone()
    }

    override fun onTaskStarted(taskId: Int, time: Long) {
        scopeLister?.onTaskStarted(taskId, time)
    }

    override fun onTaskWorking(taskId: Int, progress: Float) {
        scopeLister?.onTaskWorking(taskId, progress)
    }

    override fun onTaskDone(taskId: Int, time: Long) {
        scopeLister?.onTaskDone(taskId, time)
    }
}