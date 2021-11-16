package com.ragner.androidthreads

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ragner.androidthreads.scope.TaskScopeListener
import com.ragner.androidthreads.vm.MainViewModel

class MainActivity : AppCompatActivity(), TaskScopeListener {
    private var spinnerThreadType: Spinner? = null
    private var seekbarMaxThreads: SeekBar? = null
    private var threadsView: ThreadsView? = null
    private var checkboxYield: CheckBox? = null
    private var startButton: Button? = null

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.scopeLister = this

        configViewListeners()
    }

    private fun configViewListeners() {
        spinnerThreadType = findViewById<Spinner>(R.id.spinner_threadtype)
        seekbarMaxThreads = findViewById(R.id.seekbar_maxthreads)
        threadsView = findViewById(R.id.threadsView)
        checkboxYield = findViewById(R.id.checkbox_yield)
        startButton = findViewById(R.id.button_start)

        seekbarMaxThreads?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                threadsView?.let {
                    it.maxThreads = progress
                    it.postInvalidate()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {  }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {  }
        })

        seekbarMaxThreads?.progress = 0
    }

    private fun getSelectedThreadType() : Int = spinnerThreadType?.selectedItemPosition ?: -1

    private fun getMaxThreads() : Int = seekbarMaxThreads?.progress ?: 0

    fun start(view: View) {
        if (view is Button) {
            if (view.text == getText(R.string.start)) {
                val maxThreads = getMaxThreads()
                if (maxThreads > 0) {
                    onStarted()

                    viewModel.sleepEnabled = checkboxYield?.isChecked ?: true
                    viewModel.doStart(maxThreads, getSelectedThreadType())
                } else {
                    showWarning("Max number of threads missing!!!")
                }
            } else {
                viewModel.doCancel()
            }
        }
    }

    private fun onStarted() {
        spinnerThreadType?.isEnabled = false
        seekbarMaxThreads?.isEnabled = false
        checkboxYield?.isEnabled = false

        startButton?.text = getText(R.string.cancel)
    }

    private fun onDone() {
        onCanceled()
    }

    private fun onCanceled() {
        spinnerThreadType?.isEnabled = true
        seekbarMaxThreads?.isEnabled = true
        checkboxYield?.isEnabled = true

        startButton?.text = getText(R.string.start)
    }

    private fun showWarning(text: String) {
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    override fun onScopeStarted(maxTasks: Int) {
        threadsView?.onScopeStarted(maxTasks)
    }

    override fun onScopeDone() {
        threadsView?.also {
            it.postDelayed(
                {
                    it.onScopeDone()
                    onDone()
                },
                500
            )
        }
    }

    override fun onTaskStarted(taskId: Int, time: Long) {
        threadsView?.onTaskStarted(taskId, time)
    }

    override fun onTaskWorking(taskId: Int, progress: Float) {
        threadsView?.onTaskWorking(taskId, progress)
    }

    override fun onTaskDone(taskId: Int, time: Long) {
        threadsView?.onTaskDone(taskId, time)
    }
}