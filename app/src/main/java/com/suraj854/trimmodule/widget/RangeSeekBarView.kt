package com.suraj854.videotrimmerview.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.suraj854.trimmodule.R
import com.suraj854.trimmodule.utilis.VideoTrimmerUtil
import com.suraj854.trimmodule.widget.DateUtil
import com.suraj854.videotrimmerview.utilis.UnitConverter
import com.suraj854.videotrimmerview.widget.RangeSeekBarView
import kotlinx.coroutines.flow.flow
import java.text.DecimalFormat

class RangeSeekBarView : View {
    private var mActivePointerId = INVALID_POINTER_ID
    private var mMinShootTime = VideoTrimmerUtil.MIN_SHOOT_DURATION
    private var absoluteMinValuePrim = 0.0
    private var absoluteMaxValuePrim = 0.0
    private var normalizedMinValue = 0.0
    private var normalizedMaxValue = 1.0
    private var normalizedMinValueTime = 0.0
    private var normalizedMaxValueTime = 1.0
    private var mScaledTouchSlop = 0
    private var thumbImageLeft: Bitmap? = null
    private var thumbArrowImageLeft: Bitmap? = null
    private var thumbArrowImageRight: Bitmap? = null
    private var thumbImageRight: Bitmap? = null
    private var thumbPressedImage: Bitmap? = null
    private var paint: Paint? = null
    private var rectPaint: Paint? = null
    private val mVideoTrimTimePaintL = Paint()
    private val mVideoTrimTimePaintR = Paint()
    private val mShadow = Paint()
    private var thumbWidth = 0
    private var thumbHalfWidth = 0f
    private val padding = 0f
    private var mStartPosition: Long = 0
    private var mEndPosition: Long = 0
    private val thumbPaddingTop = 0f
    private var isTouchDown = false
    private var mDownMotionX = 0f
    private var mIsDragging = false
    private var pressedThumb: Thumb? = null
    private var isMin = false
    private var min_width = 1.0


    var isNotifyWhileDragging = false
    private var mRangeSeekBarChangeListener: OnRangeSeekBarChangeListener? = null
    private val whiteColorRes = context.resources.getColor(R.color.white)

    enum class Thumb {
        MIN, MAX
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    constructor(context: Context?, absoluteMinValuePrim: Long, absoluteMaxValuePrim: Long) : super(
        context
    ) {
        this.absoluteMinValuePrim = absoluteMinValuePrim.toDouble()
        this.absoluteMaxValuePrim = absoluteMaxValuePrim.toDouble()
        isFocusable = true
        isFocusableInTouchMode = true
        init()
    }

    private var shouldRedrawImmediately = false


    private fun init() {
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        thumbImageLeft = BitmapFactory.decodeResource(resources, R.drawable.ic_video_thumb_handle)
        thumbArrowImageLeft =
            BitmapFactory.decodeResource(resources, R.drawable.arrow_left)
        thumbArrowImageRight =
            BitmapFactory.decodeResource(resources, R.drawable.arrow_right)
        val width = thumbImageLeft?.getWidth()
        val height = thumbImageLeft?.getHeight()

        val newWidth = UnitConverter().dpToPx(15)
        val newHeight = UnitConverter().dpToPx(60)
        val scaleWidth = newWidth * 1.0f / width!!
        val scaleHeight = newHeight * 1.0f / height!!
        val matrix = Matrix()
        val matrixss = Matrix()
        matrixss.postScale(
            newWidth * 1.0f / thumbArrowImageLeft!!.width,
            UnitConverter().dpToPx(55) * 1.0f / thumbArrowImageLeft!!.height
        )
        matrix.postScale(scaleWidth, scaleHeight)

        thumbImageLeft = Bitmap.createBitmap(thumbImageLeft!!, 0, 0, width, height, matrix, true)
        thumbImageRight = thumbImageLeft
        thumbPressedImage = thumbImageLeft
        //thumbPressedImage = thumbArrowImageLeft
        thumbWidth = newWidth
        thumbHalfWidth = (thumbWidth / 2).toFloat()
        val shadowColor = context.resources.getColor(R.color.shadow_color)
        mShadow.isAntiAlias = true
        mShadow.color = shadowColor
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rectPaint!!.style = Paint.Style.FILL
        rectPaint!!.color = context.resources.getColor(R.color.colorAccent)
        mVideoTrimTimePaintL.strokeWidth = 3f
        mVideoTrimTimePaintL.setARGB(255, 51, 51, 51)
        mVideoTrimTimePaintL.textSize = 42f
        mVideoTrimTimePaintL.isAntiAlias = true
        mVideoTrimTimePaintL.color = context.resources.getColor(R.color.red)
        mVideoTrimTimePaintL.textAlign = Paint.Align.LEFT
        mVideoTrimTimePaintR.strokeWidth = 3f
        mVideoTrimTimePaintR.setARGB(255, 51, 51, 51)
        mVideoTrimTimePaintR.textSize = 42f
        mVideoTrimTimePaintR.isAntiAlias = true
        mVideoTrimTimePaintR.color = context.resources.getColor(R.color.red)
        mVideoTrimTimePaintR.textAlign = Paint.Align.RIGHT

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 300
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec)
        }
        var height = 200
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = MeasureSpec.getSize(heightMeasureSpec)

        }

        setMeasuredDimension(width, height)
    }

    fun getNormalisedValues(): kotlinx.coroutines.flow.Flow<Long> = flow<Long> {
        emit(normalizedMinValue.toLong())
    }

    var rangeL: Float = 0f
    var rangeR: Float = 0f

    var thumbLeftPosition = 0f
    var thumbRigtPosition = 1000f


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rangeL = normalizedToScreen(thumbNormalizedMinValue)
        rangeR = normalizedToScreen(thumbNormalizedMaxValue)


        val bg_middle_left = 0f
        val bg_middle_right = (width - paddingRight).toFloat()
        val leftRect =
            Rect(bg_middle_left.toInt(), height - UnitConverter().dpToPx(19), rangeL.toInt(), 0)
        val rightRect =
            Rect(rangeR.toInt(), height - UnitConverter().dpToPx(19), bg_middle_right.toInt(), 0)
        canvas.drawRect(leftRect, mShadow)
        canvas.drawRect(rightRect, mShadow)

        canvas.drawRect(
            rangeL,
            thumbPaddingTop + Companion.paddingTop,
            rangeR,
            thumbPaddingTop + UnitConverter().dpToPx(14) + Companion.paddingTop,
            rectPaint!!
        )
        canvas.drawRect(
            rangeL,
            height.toFloat() - UnitConverter().dpToPx(18),
            rangeR,
            height.toFloat() - UnitConverter().dpToPx(20),
            rectPaint!!
        )
        /*   canvas.drawRect(
               rangeL,
               (height - UnitConverter().dpToPx(4f)).toFloat(),
               rangeR-10f,
               height.toFloat()-10f,
               rectPaint!!
           )*/

        /*
                canvas.drawRect(
                    rangeL,
                    thumbPaddingTop + Companion.paddingTop,
                    rangeR,
                    thumbPaddingTop + UnitConverter().dpToPx(2) + Companion.paddingTop,
                    rectPaint!!
                )*/

        if (mIsDragging) {
            mRangeSeekBarChangeListener?.onDragNormaliseValuesChanged(
                false, thumbNormalizedMinValue,
                thumbNormalizedMaxValue
            )
        }

        mRangeSeekBarChangeListener?.onNormaliseValuesChanged(
            normalizedToScreen(normalizedMinValue),
            normalizedToScreen(normalizedMaxValue)
        )

        drawThumb(normalizedToScreen(thumbNormalizedMinValue), false, canvas, true)
        drawThumb(normalizedToScreen(thumbNormalizedMaxValue), false, canvas, false)

        drawVideoTrimTimeText(canvas)
    }

    var isPressedThumb = true
    fun drawRectangle(left: Int, top: Int, right: Int, bottom: Int, canvas: Canvas, paint: Paint?) {
        var right = right
        var bottom = bottom
        right = left + right // width is the distance from left to right
        bottom = top + bottom // height is the distance from top to bottom
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint!!)
    }

    private val handler = Handler()
    private fun drawThumb(screenCoord: Float, pressed: Boolean, canvas: Canvas, isLeft: Boolean) {
        canvas.drawBitmap(
            (if (pressed) thumbPressedImage else if (isLeft) thumbImageLeft else thumbImageRight)!!,
            screenCoord - if (isLeft) 0 else thumbWidth,
            Companion.paddingTop.toFloat(),
            paint
        )
        canvas.drawBitmap(
            (if (pressed) thumbPressedImage else if (isLeft) thumbArrowImageLeft else thumbArrowImageRight)!!,
            screenCoord - if (isLeft) 0 else thumbWidth,
            UnitConverter().dpToPx(35).toFloat(),
            paint
        )

    }

    private fun drawVideoTrimTimeText(canvas: Canvas) {
        val leftThumbsTime = DateUtil.convertSecondsToTime(mStartPosition)
        val rightThumbsTime = DateUtil.convertSecondsToTime(mEndPosition)

        canvas.drawText(
            leftThumbsTime,
            normalizedToScreen(thumbNormalizedMinValue),
            TextPositionY.toFloat(),
            mVideoTrimTimePaintL
        )


        canvas.drawText(
            rightThumbsTime,
            normalizedToScreen(thumbNormalizedMaxValue),
            TextPositionY.toFloat(),
            mVideoTrimTimePaintR
        )

    }


    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (isTouchDown) {
            return super.onTouchEvent(event)
        }
        if (event.pointerCount > 1) {
            return super.onTouchEvent(event)
        }
        if (!isEnabled) return false
        if (absoluteMaxValuePrim <= mMinShootTime) {

            return super.onTouchEvent(event)
        }
        val pointerIndex: Int // 记录点击点的index
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {

                mActivePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)

                pressedThumb = evalPressedThumb(mDownMotionX)


                if (pressedThumb == null) return super.onTouchEvent(event)
                isPressed = true
                onStartTrackingTouch()
                trackTouchEvent(event)

                attemptClaimDrag()
                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener!!.onRangeSeekBarValuesChanged(
                        this, selectedMinValue, selectedMaxValue, MotionEvent.ACTION_DOWN, isMin,
                        pressedThumb
                    )
                }
            }

            MotionEvent.ACTION_MOVE ->  if (pressedThumb != null) {

                if (mIsDragging) {

                    trackTouchEvent(event)
                } else {

                    // Scroll to follow the motion event
                    pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.getX(pointerIndex) // 手指在控件上点的X坐标
                    // 手指没有点在最大最小值上，并且在控件上有滑动事件
                    if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                        isPressed = true

                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }
                if (isNotifyWhileDragging && mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener!!.onRangeSeekBarValuesChanged(
                        this, selectedMinValue, selectedMaxValue, MotionEvent.ACTION_MOVE,
                        isMin, pressedThumb
                    )
                }
            }else {

            }

            MotionEvent.ACTION_UP -> {

                isPressedThumb = false
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }


                invalidate()

                if (mRangeSeekBarChangeListener != null) {
                    mRangeSeekBarChangeListener!!.onRangeSeekBarValuesChanged(
                        this, selectedMinValue, selectedMaxValue, MotionEvent.ACTION_UP, isMin,
                        pressedThumb
                    )
                }

                pressedThumb = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {

                val index = event.pointerCount - 1

                mDownMotionX = event.getX(index)
                mActivePointerId = event.getPointerId(index)
                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> {

                onSecondaryPointerUp(event)
                invalidate()
            }

            MotionEvent.ACTION_CANCEL -> {

                if (mIsDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate()
            }

            else -> {}
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mDownMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun trackTouchEvent(event: MotionEvent) {

        if (event.pointerCount > 1) return

        val pointerIndex = event.findPointerIndex(mActivePointerId)
        var x = 0f
        x = try {
            event.getX(pointerIndex)
        } catch (e: Exception) {

            return
        }
        if (Thumb.MIN == pressedThumb) {

            setNormalizedMinValue(screenToNormalized(x, 0))



        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(screenToNormalized(x, 1))

            thumbRigtPosition = normalizedToScreen(normalizedMaxValue)


        }
    }

    private fun screenToNormalized(screenCoord: Float, position: Int): Double {
        val width = width
        return if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            0.0
        } else {
            isMin = false
            var current_width = screenCoord.toDouble()
            val rangeL = normalizedToScreen(thumbNormalizedMinValue)
            val rangeR = normalizedToScreen(thumbNormalizedMaxValue)
            val min =
                mMinShootTime / (absoluteMaxValuePrim - absoluteMinValuePrim) * (width - thumbWidth * 2)
            min_width = if (absoluteMaxValuePrim > 5 * 60 * 1000) {
                val df = DecimalFormat("0.0000")
                df.format(min).toDouble()
            } else {
                Math.round(min + 0.5).toDouble()
            }
            if (position == 0) {
                if (isInThumbRangeLeft(screenCoord, thumbNormalizedMinValue, 0.5)) {


                    return thumbNormalizedMinValue
                }

                val rightPosition: Float =
                    if (getWidth() - rangeR >= 0) getWidth() - rangeR else 0F
                /*                val left_length = valueLength - (rightPosition + min_width)*/
                val left_length = valueLength - (rightPosition + 335.0)

                if (current_width > rangeL) {
                    current_width = rangeL + (current_width - rangeL)


                } else if (current_width <= rangeL) {
                    current_width = rangeL - (rangeL - current_width)

                }
                if (current_width > left_length) {
                    isMin = true
                    current_width = left_length


                }
                if (current_width < thumbWidth * 2 / 3) {

                    current_width = 0.0
                }
                val resultTime = (current_width - padding) / (width - 2 * thumbWidth)
                normalizedMinValueTime = Math.min(1.0, Math.max(0.0, resultTime))
                val result = (current_width - padding) / (width - 2 * padding)
                Math.min(1.0, Math.max(0.0, result))

                Math.min(1.0, Math.max(0.0, result))

            } else {
                if (isInThumbRange(screenCoord, thumbNormalizedMaxValue, 0.5)) {
                    return thumbNormalizedMaxValue
                }
                val right_length = valueLength - (rangeL + min_width)
                if (current_width > rangeR) {
                    current_width = rangeR + (current_width - rangeR)
                } else if (current_width <= rangeR) {
                    current_width = rangeR - (rangeR - current_width)
                }
                var paddingRight = getWidth() - current_width
                if (paddingRight > right_length) {
                    isMin = true
                    current_width = getWidth() - right_length
                    paddingRight = right_length
                }
                if (paddingRight < thumbWidth * 2 / 3) {
                    current_width = getWidth().toDouble()
                    paddingRight = 0.0
                }
                var resultTime = (paddingRight - padding) / (width - 2 * thumbWidth)
                resultTime = 1 - resultTime
                normalizedMaxValueTime = Math.min(1.0, Math.max(0.0, resultTime))
                val result = (current_width - padding) / (width - 2 * padding)
                Math.min(1.0, Math.max(0.0, result))
            }
        }
    }

    private val valueLength: Int
        private get() = width - 2 * thumbWidth


    private fun evalPressedThumb(touchX: Float): Thumb? {
        var result: Thumb? = null
        val minThumbPressed = isInThumbRange(touchX, thumbNormalizedMinValue, 2.0)
        val maxThumbPressed = isInThumbRange(touchX, thumbNormalizedMaxValue, 2.0)
        if (minThumbPressed && maxThumbPressed) {

            result = if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
        } else if (minThumbPressed) {
            result = Thumb.MIN
        } else if (maxThumbPressed) {
            result = Thumb.MAX
        }
        return result
    }

    private fun isInThumbRange(
        touchX: Float,
        normalizedThumbValue: Double,
        scale: Double
    ): Boolean {

        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth * scale
    }

    private fun isInThumbRangeLeft(
        touchX: Float,
        normalizedThumbValue: Double,
        scale: Double
    ): Boolean {

        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue) - thumbWidth) <= thumbHalfWidth * scale
    }


    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    fun onStartTrackingTouch() {
        mIsDragging = true
    }

    fun onStopTrackingTouch() {
        mIsDragging = false
    }

    fun setMinShootTime(min_cut_time: Long) {
        mMinShootTime = min_cut_time
    }


    fun normalizedToScreen(normalizedCoord: Double): Float {
        return (paddingLeft + normalizedCoord * (width - paddingLeft - paddingRight)).toFloat()
    }

    private fun valueToNormalized(value: Long): Double {
        return if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            0.0
        } else (value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim)
    }

    fun setStartEndTime(start: Long, end: Long) {

        mStartPosition = start / 1000
        mEndPosition = end / 1000


    }

    fun setNormalizedMinValue(value: Double) {

        thumbNormalizedMinValue =
            Math.max(0.0, Math.min(1.0, Math.min(value, thumbNormalizedMaxValue)))
        invalidate()
    }

    var thumbNormalizedMinValue: Double = 0.0
    var thumbNormalizedMaxValue: Double = 1.0
    fun setNormalizedMaxValue(value: Double) {
        thumbNormalizedMaxValue =
            Math.max(0.0, Math.min(1.0, Math.max(value, thumbNormalizedMinValue)))

        invalidate()
    }

    var selectedMinValue: Long
        get() = normalizedToValue(normalizedMinValueTime)
        set(value) {
            if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {

                setNormalizedMinValue(0.0)
            } else {
                setNormalizedMinValue(valueToNormalized(value))
            }
        }
    var resetSelectedMinValue: Boolean
        get() = true
        set(value) {

            shouldRedrawImmediately = value
        }
    var count = 1
    fun readTrimmer(left: Double, right: Double) {


        thumbNormalizedMaxValue = right
        thumbNormalizedMinValue = left

        postInvalidate()


    }


    var selectedNormalizedMinValue: Float
        get() = normalizedToScreen(normalizedMinValue)
        set(value) {
            normalizedToScreen(normalizedMinValue)
        }

    var selectedMaxValue: Long
        get() = normalizedToValue(normalizedMaxValueTime)
        set(value) {
            if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {

                setNormalizedMaxValue(1.0)
            } else {

                setNormalizedMaxValue(valueToNormalized(value))
            }
        }

    private fun normalizedToValue(normalized: Double): Long {
        return (absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim)).toLong()
    }

    fun setTouchDown(touchDown: Boolean) {
        isTouchDown = touchDown
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putDouble("MIN", normalizedMinValue)
        bundle.putDouble("MAX", normalizedMaxValue)
        bundle.putDouble("MIN_TIME", normalizedMinValueTime)
        bundle.putDouble("MAX_TIME", normalizedMaxValueTime)
        return bundle
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        normalizedMinValue = bundle.getDouble("MIN")
        normalizedMaxValue = bundle.getDouble("MAX")
        normalizedMinValueTime = bundle.getDouble("MIN_TIME")
        normalizedMaxValueTime = bundle.getDouble("MAX_TIME")
    }

    interface OnRangeSeekBarChangeListener {
        fun onRangeSeekBarValuesChanged(
            bar: RangeSeekBarView?,
            minValue: Long,
            maxValue: Long,
            action: Int,

            isMin: Boolean,
            pressedThumb: Thumb?
        )

        fun onNormaliseValuesChanged(
            min: Float,
            max: Float
        )

        fun onDragNormaliseValuesChanged(
            isPressed: Boolean,
            min: Double,
            max: Double
        )
    }

    fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener?) {
        mRangeSeekBarChangeListener = listener
    }

    companion object {
        private val TAG = RangeSeekBarView::class.java.simpleName
        const val INVALID_POINTER_ID = 255
        const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        const val ACTION_POINTER_INDEX_SHIFT = 8
        private val TextPositionY = UnitConverter().dpToPx(90f)
        private val paddingTop = UnitConverter().dpToPx(10)
    }
}