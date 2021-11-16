package com.ragner.androidthreads

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.ragner.androidthreads.scope.TaskScopeListener
import kotlin.math.*

class ThreadsView(context: Context?, attrs: AttributeSet?) : View(context, attrs), TaskScopeListener {
    private val MAX_THREADS = 100

    private var mCellSize = 100f

    private val mThreadArray = FloatArray(MAX_THREADS) { 0f }
    private val mValueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 360f)

    private val mPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    var maxThreads: Int = 0
        set(value) {
            field = if (value > MAX_THREADS) MAX_THREADS else value
        }

    init {
        mValueAnimator.apply {
            startDelay = 1000L
            duration = 1000L
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                if (!isDirty) {
                    postInvalidate()
                }
            }

            start()
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val cw = ceil(sqrt((MAX_THREADS * w / h).toFloat()))
        val ch = ceil(sqrt((MAX_THREADS * h / w).toFloat()))

        mCellSize = if (cw < ch) w / cw else h / ch
        mPath = newShapePath(6, mCellSize * 0.4f)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var x = 0f
        var y = 0f
        var padding: Float = mCellSize * 0.1f

        if (canvas == null) {
            return
        }

        canvas.drawColor(Color.DKGRAY)

        repeat(maxThreads) {

            when (mThreadArray[it]) {
                0f -> {
                    onDrawIdle(canvas,x + padding, y + padding,
                        mCellSize - padding, mCellSize - padding,
                        mPaint, mThreadArray[it]
                    )
                }
                1f -> {
                    onDrawDone(canvas,x + padding, y + padding,
                        mCellSize - padding, mCellSize - padding,
                        mPaint, mThreadArray[it]
                    )
                }
                else -> {
                    onDrawAlive(canvas,x + padding, y + padding,
                        mCellSize - padding, mCellSize - padding,
                        mPaint, mThreadArray[it]
                    )
                }
            }

            x += mCellSize

            if (x + mCellSize > width) {
                x = 0f
                y += mCellSize
            }
        }
    }

    private fun onDrawIdle(canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
                           p: Paint, progress: Float) {
        p.color = Color.GREEN
        p.style = Paint.Style.STROKE
        canvas.drawRect(x, y, x + w, y + h, p)
    }

    private fun onDrawDone(canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
                           p: Paint, progress: Float) {
        p.color = Color.GREEN
        p.style = Paint.Style.FILL_AND_STROKE
        canvas.drawRect(x, y, x + w, y + h, p)
    }

    var mPath = newShapePath(6, mCellSize * 0.4f)
    private fun onDrawAlive(canvas: Canvas, x: Float, y: Float, w: Float, h: Float,
                            p: Paint, progress: Float) {
        p.color = Color.GREEN
        p.style = Paint.Style.STROKE
        canvas.drawRect(x, y, x + w, y + h, p)

        p.style = Paint.Style.FILL
        canvas.drawRect(x, y, x + w * progress, y + h, p)

        canvas.save()

        p.color = Color.YELLOW
        p.style = Paint.Style.FILL

        canvas.translate(x + w * 0.5f, y + h * 0.5f)
        canvas.rotate(2 * progress * 360f, 0f, 0f)
        canvas.drawPath(mPath, p)

        canvas.restore()
    }

    private fun newShapePath(count: Int, radius: Float) : Path {
        val path = Path()
        val radian = 2f * PI.toFloat()

        path.moveTo(radius * cos (0f), radius * sin (0f))

        for (i in 1 until count) {
            path.lineTo(radius * cos(radian / count*i),radius * sin(radian / count*i))
        }

        path.close()

        return path
    }

    private fun onScopeIdle() {
        repeat(MAX_THREADS) {
            mThreadArray[it] = 0f
        }
    }

    override fun onScopeStarted(maxTasks: Int) {
        maxThreads = maxTasks

        onScopeIdle()
    }

    override fun onScopeDone() {
    }

    override fun onTaskStarted(taskId: Int, time: Long) {
        if (taskId < maxThreads) {
            mThreadArray[taskId] = 0f
        }
    }

    override fun onTaskWorking(taskId: Int, progress: Float) {
        mThreadArray[taskId] = progress
    }

    override fun onTaskDone(taskId: Int, time: Long) {
        if (taskId < maxThreads) {
            mThreadArray[taskId] = 1f
        }
    }
}