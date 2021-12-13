package com.android.mca2021.keyboard

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import kotlin.math.atan2
import android.animation.Animator

import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet


class CircularButton(context: Context?, attrs: AttributeSet?, defStyle: Int) :
    View(context, attrs, defStyle) {

    var currentEmoji = 0
    var adjecentEmojis = arrayOfNulls<Int>(5)

    private var mWidth = 0
    private var mHeight = 0

    private val mSliceNum = 5
    private var mSlices = ArrayList<mSlice>()

    private var mOuterRadius = 0f
    private var mInnerRadius = 0f
    private val innerRadiusRatio = 0.3f
    private var circleRadius = 0f
    private val degreeStep :Float = 180f/mSliceNum
    private var currentStartingDegree = 0f

    private var mCenterX = 0
    private var mCenterY = 0
    private var mPressed = false
    private var mPressedButton = -1
    private var mPrevPressedButton = -1
    private val animDuration : Long= 200


    class mSlice(var degreeStep: Float, var centerDegree: Float, var radius: Float, private val centerX: Int, private val centerY: Int){
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mRectF = RectF()
        var isPressed = false
        var isSelected = false

        fun draw(canvas: Canvas){
            mRectF.left = (centerX - radius)
            mRectF.right = (centerX + radius)
            mRectF.top = (centerY - radius)
            mRectF.bottom = (centerY + radius)

            mPaint.strokeWidth = 5f
            mPaint.style = Paint.Style.STROKE
            mPaint.color = Color.WHITE

            canvas.drawArc(mRectF, centerDegree - degreeStep/2, degreeStep, true, mPaint)

            mPaint.style = Paint.Style.FILL
            if(isPressed || isSelected){
                mPaint.color = Color.WHITE
                mPaint.alpha = 100
            }
            else{
                mPaint.color = Color.DKGRAY
                mPaint.alpha = 100
            }
            canvas.drawArc(mRectF, centerDegree - degreeStep/2, degreeStep, true, mPaint)
            Log.d("draw", "drew at ${centerDegree - 180}, with step ${degreeStep}!")


        }
    }

    private lateinit var spinAnim_reverse: ValueAnimator

    private val spinAnim = ValueAnimator.ofFloat(30f, 180f).apply {
        duration = (animDuration * 2).toLong()
        interpolator = OvershootInterpolator(1.5f)
        addUpdateListener { updatedAnim ->
            val value = updatedAnim.animatedValue
            currentStartingDegree = value as Float
            for(i in 0 until mSliceNum) {
                mSlices[i].centerDegree = value + degreeStep/2 + (degreeStep * i)
            }
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Log.d("draw", "spinDone at ${System.currentTimeMillis()}!")
            }
        })
    }


    private lateinit var expandAnim: ValueAnimator
    private lateinit var expandAnim_reverse: ValueAnimator
    private lateinit var expandAnim_reverseOthers: ValueAnimator
    private lateinit var expandAnim_circle: ValueAnimator
    private lateinit var expandAnim_circleReverse: ValueAnimator
    private lateinit var expandAnim_circleBig: ValueAnimator
    private lateinit var expandAnim_circleBigReverse: ValueAnimator
    private lateinit var circleSelectedAnim : AnimatorSet

    private var spreadAnim = ValueAnimator.ofFloat(degreeStep, degreeStep * 1.2f).apply{
        duration = (animDuration * 1.5).toLong()
        addUpdateListener { updatedAnim ->
            for(i in 0 until mSliceNum){
                if(mSlices[i].isPressed){
                    val value = updatedAnim.animatedValue as Float
                    val diff = (value - degreeStep)
                    mSlices[i].degreeStep = value
                    if(i-1 >= 0){
                        mSlices[i-1].degreeStep = degreeStep - diff/2f

                        mSlices[i-1].centerDegree = mSlices[i].centerDegree - mSlices[i].degreeStep/2 - mSlices[i-1].degreeStep/2
                    }
                    if(i+1 < mSliceNum){
                        mSlices[i+1].degreeStep = degreeStep - diff/2f
                        mSlices[i+1].centerDegree = mSlices[i].centerDegree + mSlices[i].degreeStep/2 + mSlices[i+1].degreeStep/2
                    }
                }
            }
            invalidate()
        }
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    private fun isAnimFinished(): Boolean{
        val anims = listOf<ValueAnimator>(
            spinAnim,
            expandAnim,
            expandAnim_reverse,
            expandAnim_reverseOthers,
            expandAnim_circle,
            expandAnim_circleReverse,
            expandAnim_circleBig,
            spreadAnim
        )
        var finished = true
        for(anim in anims){
            finished = finished && !anim.isStarted
        }
        if(this::spinAnim_reverse.isInitialized){
            finished = finished && !spinAnim_reverse.isStarted
        }
        return finished
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w
        mHeight = h

        /* center */
        mCenterX = w / 2
        mCenterY = (h * 0.8).toInt()

        /* select shorter one from width & height as diameter */
        mOuterRadius = if (w>h) (h/2).toFloat() else (w/2).toFloat()
        mOuterRadius = mOuterRadius * 0.8f
        mInnerRadius = mOuterRadius * innerRadiusRatio
        circleRadius = mInnerRadius

        expandAnim = ValueAnimator.ofFloat(mOuterRadius, mOuterRadius*1.2f).apply {
            duration = (animDuration*1.5).toLong()
            interpolator = OvershootInterpolator()
            addUpdateListener { updatedAnim ->
                for(i in 0 until mSliceNum){
                    if(mSlices[i].isPressed)
                        mSlices[i].radius = updatedAnim.animatedValue as Float
                }
                invalidate()
            }
        }

        expandAnim_reverse = ValueAnimator.ofFloat(mOuterRadius*1.2f, 0f).apply {
            duration = animDuration
            interpolator = AnticipateInterpolator()
            addUpdateListener { updatedAnim ->
                for(i in 0 until mSliceNum){
                    if(mSlices[i].isSelected)
                        mSlices[i].radius = updatedAnim.animatedValue as Float
                }
                invalidate()
            }
        }

        expandAnim_reverseOthers = ValueAnimator.ofFloat(mOuterRadius, 0f).apply {
            duration = animDuration
            addUpdateListener { updatedAnim ->
                for(i in 0 until mSliceNum){
                    if(!mSlices[i].isSelected)
                        mSlices[i].radius = updatedAnim.animatedValue as Float
                }
                invalidate()
            }
        }

        expandAnim_circle = ValueAnimator.ofFloat(mInnerRadius, mInnerRadius * 1.2f).apply {
            duration = animDuration
            addUpdateListener { updatedAnim ->
                circleRadius = updatedAnim.animatedValue as Float
                invalidate()
            }
        }

        expandAnim_circleReverse = ValueAnimator.ofFloat(mInnerRadius*1.2f, mInnerRadius).apply {
            duration = animDuration
            addUpdateListener { updatedAnim ->
                circleRadius = updatedAnim.animatedValue as Float
                invalidate()
            }
        }

        expandAnim_circleBig = ValueAnimator.ofFloat(mInnerRadius*1.2f, mInnerRadius * 1.5f).apply {
            duration = animDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener { updatedAnim ->
                circleRadius = updatedAnim.animatedValue as Float
                invalidate()
            }
        }

        expandAnim_circleBigReverse = ValueAnimator.ofFloat(mInnerRadius*1.5f, mInnerRadius).apply {
            duration = animDuration
            interpolator = OvershootInterpolator(5f)
            addUpdateListener { updatedAnim ->
                circleRadius = updatedAnim.animatedValue as Float
                invalidate()
            }
        }

        circleSelectedAnim = AnimatorSet().apply{
            play(expandAnim_circleBigReverse).after(expandAnim_circleBig)
        }

        resetAll()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currX = event.x
        val currY = event.y
        mPressedButton = xy2index(currX, currY)
        //Log.d("mPressedButton", mPressedButton.toString())
        //Log.d("mPressed", "$mPressed")
        val sliceIndex = mPressedButton -1
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if(!mPressed){
                    if(mPressedButton == 0){
                        mPressed = true
                        spinAnim.start()
                        expandAnim_circle.start()
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if(mPressed){
                    if(mPrevPressedButton != mPressedButton){
                        resetAll()
                        spreadAnim.start()
                        expandAnim.start()
                        mPrevPressedButton = mPressedButton
                    }
                    if(sliceIndex >= 0) {
                        mSlices[sliceIndex].isPressed = true
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                spinAnim.cancel()
                if(mPressed){
                    resetAll()
                    spinAnim_reverse = ValueAnimator.ofFloat(currentStartingDegree, 0f).apply {
                        duration = animDuration
                        interpolator = DecelerateInterpolator()
                        addUpdateListener { updatedAnim ->
                            val value = updatedAnim.animatedValue as Float
                            for(i in 0 until mSliceNum) {
                                mSlices[i].centerDegree = value + mSlices[i].degreeStep/2 + (mSlices[i].degreeStep * i)
                            }
                            if(value == 0f){
                                resetAll()
                            }
                            invalidate()
                        }
                    }
                    if(mPressedButton >= 0){
                        Log.d("sliceIndex:", "$sliceIndex")
                        if(sliceIndex >= 0) {
                            /* selected slice */
                            mSlices[sliceIndex].isSelected = true
                            expandAnim_reverse.start()
                            expandAnim_circleReverse.start()
                        } else {
                            Log.d("skim", "selected circle")
                            //expandAnim_circleBig.start()
                            circleSelectedAnim.start()
                        }
                        expandAnim_reverseOthers.start()
                        //spinAnim_reverse.start()
                    } else{
                        spinAnim_reverse.start()
                        expandAnim_circleReverse.start()
                    }
                    mPressedButton = -1
                    mPrevPressedButton = -1
                    mPressed = false
                }
                circleRadius = mInnerRadius
                invalidate()
            }
        }
        return true
    }

    private fun xy2index(x: Float, y: Float): Int {
        val dx = x.toInt() - mCenterX
        val dy = y.toInt() - mCenterY
        val distanceSquare = dx * dx + dy * dy

        /* center button */
        if(distanceSquare < mInnerRadius * mInnerRadius){
            return 0
        }
        /* slice buttons, index starting from 1 */
        else if(distanceSquare < mOuterRadius * mOuterRadius){
            //get the angle to detect which slice is currently being click
            /* -PI ~ PI, starting from 3 o'clock */
            var angle = atan2(dy.toDouble(), dx.toDouble())

            /* shift to 0 ~ 2PI, starting from 9 o'clock */
            angle = (angle + Math.PI)%(2 * Math.PI)

            if(angle < Math.PI) {
                angle = Math.toDegrees(angle) + 180
                for(i in 0 until mSliceNum){
                    val mSlice = mSlices[i]
                    val start = mSlice.centerDegree - (mSlice.degreeStep/2)
                    val end = mSlice.centerDegree + (mSlice.degreeStep/2)
                    if(angle in start..end)
                        return i+1
                }
            }
        }
        return -1
    }

    override fun onDraw(canvas: Canvas) {
        if(mPressed || !isAnimFinished()){
            Log.d("draw start", "--------")
            for (i in 0 until mSliceNum)
                if(!(mSlices[i].isPressed)){
                    Log.d("draw notp", i.toString())
                    mSlices[i].draw(canvas)
                }
            for (i in 0 until mSliceNum)
                if(mSlices[i].isPressed){
                    Log.d("draw yesp", i.toString())
                    mSlices[i].draw(canvas)
                }
        }

        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.BLACK
        canvas.drawRect(0F, mCenterY.toFloat(), mWidth.toFloat(), mHeight.toFloat(), mPaint)

        drawCircle(canvas)
    }

    private fun resetAll(){
        mSlices.clear()
        for(i in 0 until mSliceNum)
            mSlices.add(
                mSlice(
                    degreeStep,
                    180 + degreeStep / 2 + (degreeStep * i),
                    mOuterRadius,
                    mCenterX,
                    mCenterY
                )
            )
    }

    private fun drawCircle(canvas: Canvas){
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 5F
        mPaint.color = Color.WHITE
        canvas.drawCircle(mCenterX.toFloat(), mCenterY.toFloat(), circleRadius, mPaint)
    }

    init{
        resetAll()
    }

}
