package streaming.yeah1.com.streaming.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ProgressBar
import streaming.yeah1.com.streaming.R

/**
 * 视频录制进度条
 */

class RecordingProgressBar : ProgressBar {

    private var heightBar: Int = 0
    private var widthBar: Int = 0
    private var startIndex: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        heightBar = measuredHeight
        widthBar = measuredWidth
        startIndex = widthBar / 5
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint()
        paint.color = ContextCompat.getColor(context, R.color.white)
        canvas.drawRect(startIndex.toFloat(), 0f, (startIndex + 10).toFloat(), heightBar.toFloat(), paint)
    }
}
