package com.android.mca2021.keyboard

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import android.animation.Animator

import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.widget.Toast
import com.android.mca2021.keyboard.core.FaceAnalyzer
import com.seonjunkim.radialmenu.EmojiGraph


class PieMenu(context: Context?, attrs: AttributeSet?, defStyle: Int) :
    View(context, attrs, defStyle) {

    var mCurrentEmojiId = -1
    private var mPrevEmojiId = -1
    private var mEmojiUpdated = false
    var mPlatform: EmojiPlatform = EmojiPlatform.GOOGLE
    val emojiGraph = EmojiGraph()

    var inputConnection: InputConnection? = null

    private lateinit var faceAnalyzer: FaceAnalyzer

    /* scales */
    var mTotalScale = 0.8f
    var mCircleEmojiScale = 1f
    var mCircleRadiusScale = 1f
    var mSliceEmojiScale = 1f
    var mSliceRadiusScale = 1f

    private var mWidth = 0
    private var mHeight = 0

    private val mSliceNum = 5
    private var mSlices = ArrayList<mSlice>()

    private var mOuterRadius = 0f
    private var mInnerRadius = 0f
    private val innerRadiusRatio = 0.3f
    private var circleRadius = 0f
    private var circleEmojiSize = 0f
    private var barSymbolSize = 0f
    private val degreeStep :Float = 180f/mSliceNum
    private var currentStartingDegree = 0f

    private var mCenterX = 0F
    private var mCenterY = 0F
    private var mPressed = false
    private var mIsTraversing = false
    private var mPressedButton = -1
    private var mPrevPressedButton = -1
    private val animDuration : Long= 200

    private var bgAlphaDefault = 150
    private var bgAlpha = bgAlphaDefault


    class mSlice(val context: Context, var degreeStep: Float, var centerDegree: Float, var radius: Float, var emojiScale: Float, private val centerX: Float, private val centerY: Float, val platform: String){
        var isPressed = false
        var isSelected = false

        var mEmojiId = -1

        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mRectF = RectF()

        fun draw(canvas: Canvas){
            mRectF.left = (centerX - radius)
            mRectF.right = (centerX + radius)
            mRectF.top = (centerY - radius)
            mRectF.bottom = (centerY + radius)

            if(mEmojiId != -1){
                /* draw slice line */
                mPaint.strokeWidth = 5f
                mPaint.style = Paint.Style.STROKE
                mPaint.color = Color.WHITE
                //canvas.drawArc(mRectF, centerDegree - degreeStep/2, degreeStep, true, mPaint)

                /* draw slice body */
                mPaint.style = Paint.Style.FILL
                mPaint.color = if(isPressed || isSelected) Color.WHITE else Color.GRAY
                mPaint.alpha = 150
                canvas.drawArc(mRectF, centerDegree - degreeStep/2, degreeStep, true, mPaint)


                /* draw emoji */
                val eCenterX = centerX - radius * 0.75 * cos(Math.toRadians(((centerDegree-180)).toDouble()))
                val eCenterY = centerY - radius * 0.75 * sin(Math.toRadians(((centerDegree-180)).toDouble()))
                val id = context.resources.getIdentifier("zzz_${platform.lowercase()}_${mEmojiId}", "drawable", context.packageName)
                var bmp = BitmapFactory.decodeResource(context.resources, id)
                var mRectF =RectF()

                val mEmojiSize = emojiScale* bmp.width*degreeStep*radius/(5*36*450)
                mRectF.left = (eCenterX - mEmojiSize).toFloat()
                mRectF.right = (eCenterX + mEmojiSize).toFloat()
                mRectF.top = (eCenterY - mEmojiSize).toFloat()
                mRectF.bottom = (eCenterY + mEmojiSize).toFloat()
                canvas.drawBitmap(bmp, null, mRectF, null)
            }


        }
    }

    private val spinAnim = ValueAnimator.ofFloat(30f, 180f).apply {
        duration = (animDuration * 2)
        interpolator = OvershootInterpolator(1.5f)
        addUpdateListener { updatedAnim ->
            val value = updatedAnim.animatedValue
            currentStartingDegree = value as Float
            for(i in 0 until mSliceNum) {
                mSlices[i].centerDegree = value + degreeStep/2 + (degreeStep * i)
            }
            invalidate()
        }
    }
    private lateinit var spinAnim_reverse: ValueAnimator

    private lateinit var expandAnim: ValueAnimator
    private lateinit var expandAnim_reverse: ValueAnimator
    private lateinit var expandAnim_reverseTo0: ValueAnimator
    private lateinit var expandAnim_reverseOthersTo0: ValueAnimator
    private lateinit var expandAnim_circle: ValueAnimator
    private lateinit var expandAnim_circleReverse: ValueAnimator
    private lateinit var expandAnim_circleBig: ValueAnimator
    private lateinit var expandAnim_circleBigReverse: ValueAnimator
    private lateinit var expandAnim_emoji: ValueAnimator
    private lateinit var expandAnim_barSymbol: ValueAnimator
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

    private val alphaAnim_toLower = ValueAnimator.ofInt(bgAlphaDefault, 0).apply {
        duration = animDuration
        addUpdateListener { updatedAnim ->
            val value = updatedAnim.animatedValue
            bgAlpha = value as Int
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mEmojiUpdated = false
            }
        })
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    private fun isAnimFinished(): Boolean{
        val anims = listOf<ValueAnimator>(
            spinAnim,
            /* spinAnim_reverse -> below */
            expandAnim,
            expandAnim_reverse,
            expandAnim_reverseTo0,
            expandAnim_reverseOthersTo0,
            expandAnim_circle,
            expandAnim_circleReverse,
            expandAnim_circleBig,
            expandAnim_circleBigReverse,
            //expandAnim_emoji,
            spreadAnim,
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
        mCenterX = w / 2f
        mCenterY = (h * 0.85f)

        /* select shorter one from width & height as diameter */
        mOuterRadius = if (w>h) (h/2).toFloat() else (w/2).toFloat()
        mOuterRadius = mOuterRadius * mTotalScale
        mInnerRadius = mOuterRadius * innerRadiusRatio
        barSymbolSize = mInnerRadius * 0.3f
        circleRadius = mInnerRadius
        circleEmojiSize = circleRadius * 1.2f

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

        expandAnim_reverse = ValueAnimator.ofFloat(mOuterRadius*1.2f, mOuterRadius).apply {
            duration = (animDuration*1.5).toLong()
            interpolator = OvershootInterpolator()
            addUpdateListener { updatedAnim ->
                for(i in 0 until mSliceNum){
                    if(!mSlices[i].isPressed){
                        if(mSlices[i].radius > mOuterRadius){
                            mSlices[i].radius = updatedAnim.animatedValue as Float
                        }
                    }
                }
                invalidate()
            }
        }

        expandAnim_reverseTo0 = ValueAnimator.ofFloat(mOuterRadius*1.2f, 0f).apply {
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

        expandAnim_reverseOthersTo0 = ValueAnimator.ofFloat(mOuterRadius, 0f).apply {
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

        expandAnim_circleBig = ValueAnimator.ofFloat(mInnerRadius*1.2f, mInnerRadius * 1.3f).apply {
            duration = animDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener { updatedAnim ->
                circleRadius = updatedAnim.animatedValue as Float
                invalidate()
            }
        }

        expandAnim_circleBigReverse = ValueAnimator.ofFloat(mInnerRadius*1.3f, mInnerRadius).apply {
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

        expandAnim_emoji = ValueAnimator.ofFloat(0f, 1.2f*mInnerRadius/2f).apply {
            duration = animDuration*2
            interpolator = OvershootInterpolator()
            addUpdateListener { updatedAnim ->
                circleEmojiSize = updatedAnim.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mEmojiUpdated = false
                }
            })
        }

        /*
        expandAnim_barSymbol = ValueAnimator.ofFloat(mInnerRadius * 0.3f, mInnerRadius * 0.5f).apply {
            duration = (animDuration*1.5).toLong();
            interpolator = OvershootInterpolator()
            addUpdateListener { updatedAnim ->
                circleEmojiSize = updatedAnim.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mEmojiUpdated = false
                }
            })
        }
         */

        initSlices()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currX = event.x
        val currY = event.y
        mPressedButton = xy2index(currX, currY)
        val sliceIndex = mPressedButton -1
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("asdf", "mpressed : ${mPressedButton}")
                if(!mPressed){
                    if(mPressedButton == 0){
                        /* center circle */
                        if(mCurrentEmojiId != -1){
                            mPressed = true
                            mIsTraversing = true
                            bgAlpha = bgAlphaDefault
                            if(this::faceAnalyzer.isInitialized)
                                faceAnalyzer.pauseAnalysis()
                            updateSlices(mCurrentEmojiId)
                            spinAnim.start()
                            expandAnim_circle.start()
                        } else{
                            Toast.makeText(context, R.string.no_face, Toast.LENGTH_SHORT).show()
                        }
                    }else if (mPressedButton < 6){
                        /* background */
                        if(mIsTraversing){
                            resetSlices()
                            alphaAnim_toLower.start()
                            mIsTraversing = false
                            mCurrentEmojiId = -1
                            mPrevEmojiId = -1
                            if(this::faceAnalyzer.isInitialized)
                                faceAnalyzer.resumeAnalysis()
                        }
                    }else if (mPressedButton == 6){
                        Log.d("asdf", "right box")
                        /* right box */
                        val eventTime = SystemClock.uptimeMillis()
                        inputConnection?.finishComposingText()
                        inputConnection?.sendKeyEvent(
                            KeyEvent(
                                eventTime, eventTime,
                                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD
                            )
                        )
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                if(mPressed){
                    if(mPrevPressedButton != mPressedButton){
                        resetSlices()
                        if(sliceIndex in 0 until mSliceNum && mSlices[sliceIndex].mEmojiId != -1){
                            spreadAnim.start()
                            expandAnim.start()
                        }
                        mPrevPressedButton = mPressedButton
                    }
                    if( sliceIndex in 0 until mSliceNum && mSlices[sliceIndex].mEmojiId != -1) {
                        mSlices[sliceIndex].isPressed = true
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                spinAnim.cancel()
                if(mPressed){
                    resetSlices()
                    spinAnim_reverse = ValueAnimator.ofFloat(currentStartingDegree, 0f).apply {
                        duration = animDuration
                        interpolator = DecelerateInterpolator()
                        addUpdateListener { updatedAnim ->
                            val value = updatedAnim.animatedValue as Float
                            for(i in 0 until mSliceNum) {
                                mSlices[i].centerDegree = value + mSlices[i].degreeStep/2 + (mSlices[i].degreeStep * i)
                            }
                            if(value == 0f){
                                resetSlices()
                            }
                            invalidate()
                        }
                    }

                    if(mPressedButton in 0..mSliceNum){
                        if(sliceIndex in 0 until mSliceNum) {
                            /* selected slice */
                            if(mSlices[sliceIndex].mEmojiId != -1){
                                mSlices[sliceIndex].isSelected = true
                                mPrevEmojiId = mCurrentEmojiId
                                mCurrentEmojiId = mSlices[sliceIndex].mEmojiId
                                mEmojiUpdated = true
                                expandAnim_emoji.start()
                                expandAnim_reverseTo0.start()
                                expandAnim_circleReverse.start()
                            }
                        } else {
                            /* selected circle */
                            if(this::faceAnalyzer.isInitialized)
                                faceAnalyzer.resumeAnalysis()
                            if(mCurrentEmojiId != -1)
                                inputConnection?.commitText(emojiIdtoString(mCurrentEmojiId), 1)
                            alphaAnim_toLower.start()
                            mIsTraversing = false
                            circleSelectedAnim.start()
                            mPrevEmojiId = -1
                        }
                        expandAnim_reverseOthersTo0.start()
                    } else if(mPressedButton ==7 && mPrevEmojiId != -1){
                        /* left box */
                        mCurrentEmojiId = mPrevEmojiId
                        mPrevEmojiId = -1
                        mEmojiUpdated = true
                        expandAnim_emoji.start()
                        expandAnim_reverseTo0.start()
                        expandAnim_circleReverse.start()
                        expandAnim_reverseOthersTo0.start()
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

    private fun emojiIdtoString(mCurrentEmojiId: Int): CharSequence {
        return emojiGraph.emojiIdtoString(mCurrentEmojiId)
    }

    private fun xy2index(x: Float, y: Float): Int {
        val dx = x.toInt() - mCenterX
        val dy = y.toInt() - mCenterY
        val distanceSquare = dx * dx + dy * dy

        /* center button */
        if(distanceSquare < mInnerRadius * mInnerRadius){
            return 0
        }

        //get the angle to detect which slice is currently being click
        /* -PI ~ PI, starting from 3 o'clock */
        var angle = atan2(dy.toDouble(), dx.toDouble())

        /* shift to 0 ~ 2PI, starting from 9 o'clock */
        angle = (angle + Math.PI)%(2 * Math.PI)

        /* slice buttons, index starting from 1 */
        if(angle < Math.PI ) {
            if (distanceSquare < mOuterRadius * mOuterRadius) {
                angle = Math.toDegrees(angle) + 180
                for (i in 0 until mSliceNum) {
                    val mSlice = mSlices[i]
                    val start = mSlice.centerDegree - (mSlice.degreeStep / 2)
                    val end = mSlice.centerDegree + (mSlice.degreeStep / 2)
                    if (angle in start..end)
                        return i + 1
                }
            }
        } else if(angle < Math.PI * 1.5f){
            return 6
        } else {
            return 7
        }
        return -1
    }

    @SuppressLint("DrawAllocation", "UseCompatLoadingForDrawables")
    override fun onDraw(canvas: Canvas) {
        /* bg */
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        if(mIsTraversing || alphaAnim_toLower.isStarted){
            mPaint.style = Paint.Style.FILL
            mPaint.color = Color.BLACK
            mPaint.alpha = bgAlpha
            canvas.drawRect(0F, 0F, mWidth.toFloat(), mHeight.toFloat(), mPaint)
        }

        /* slices */
        if(mPressed || !isAnimFinished()){
            for (i in 0 until mSliceNum)
                if(!(mSlices[i].isPressed)){
                    mSlices[i].draw(canvas)
                }
            for (i in 0 until mSliceNum)
                if(mSlices[i].isPressed){
                    mSlices[i].draw(canvas)
                }
        }

        /* box */
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.BLACK
        mPaint.alpha = 100
        canvas.drawRect(0F, mCenterY, mWidth.toFloat(), mHeight.toFloat(), mPaint)

        /* circle */
        drawCircle(canvas)

        /* previous emoji */
        if(mPressed){
            val prevEmojiSize = mInnerRadius/2.5f
            val margin = mInnerRadius * 0.4f
            if(mPrevEmojiId >= 0)
                drawEmoji(canvas, mPrevEmojiId, mCenterX - mInnerRadius - prevEmojiSize - margin, mHeight - (mHeight - mCenterY)/2, prevEmojiSize, 100)
        }

        /* cancel */
        if(mPressed){
            val cancelmargin: Float = mInnerRadius/2.5f + mInnerRadius*0.5f
            drawDrawable(canvas,
                context.getDrawable(R.drawable.cancel)!!, mCenterX + mInnerRadius + cancelmargin, mHeight - (mHeight - mCenterY)/2, mInnerRadius * 0.3f, 100)
        }


        /* backspace */
        val bsmargin: Float = mInnerRadius * 0.2f
        drawDrawable(canvas,
            context.getDrawable(R.drawable.backspace)!!, mWidth - mInnerRadius/2.5f - bsmargin, mHeight - (mHeight - mCenterY)/2, mInnerRadius * 0.3f)

    }

    private fun drawDrawable(canvas: Canvas, drawable: Drawable, centerX: Float, centerY: Float, size: Float, alpha: Int = 255){
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.alpha = alpha
        val cv = Canvas()
        val bmp =
            drawable.let { Bitmap.createBitmap(it.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888) }
        cv.setBitmap(bmp)
        drawable.setBounds(0,0,drawable.intrinsicWidth,drawable.intrinsicHeight)
        drawable.draw(cv)
        var mRectF = RectF()
        mRectF.left = centerX - size
        mRectF.right = centerX + size
        mRectF.top = centerY - size
        mRectF.bottom = centerY + size
        if (bmp != null) {
            canvas.drawBitmap(bmp, null, mRectF, paint)
        }

    }

    private fun resetSlices(){

        val ids = intArrayOf(
            mSlices[0].mEmojiId,
            mSlices[1].mEmojiId,
            mSlices[2].mEmojiId,
            mSlices[3].mEmojiId,
            mSlices[4].mEmojiId,
        )
        mSlices.clear()

        for(i in 0 until mSliceNum) {
            /* platform size scaling */
            var platformEmojiScale = mSliceEmojiScale
            when (mPlatform) {
                EmojiPlatform.SAMSUNG -> platformEmojiScale *= 1.1f
                EmojiPlatform.TWITTER -> platformEmojiScale *= 0.9f
            }

            mSlices.add(
                mSlice(
                    context,
                    degreeStep,
                    180 + degreeStep / 2 + (degreeStep * i),
                    mOuterRadius * mSliceRadiusScale,
                    platformEmojiScale,
                    mCenterX,
                    mCenterY,
                    mPlatform.name.lowercase()
                )
            )
        }

        for(i in 0 until mSliceNum) {
            mSlices[i].mEmojiId = ids[i]
        }
    }

    private fun initSlices(){
        mSlices.clear()
        for(i in 0 until mSliceNum) {
            /* platform size scaling */
            var platformEmojiScale = mSliceEmojiScale
            when (mPlatform) {
                EmojiPlatform.SAMSUNG -> platformEmojiScale *= 1.1f
                EmojiPlatform.TWITTER -> platformEmojiScale *= 0.9f
            }

            mSlices.add(
                mSlice(
                    context,
                    degreeStep,
                    180 + degreeStep / 2 + (degreeStep * i),
                    mOuterRadius * mSliceRadiusScale,
                    platformEmojiScale,
                    mCenterX,
                    mCenterY,
                    mPlatform.name.lowercase()
                )
            )
        }
    }

    private fun updateSlices(emojiId: Int){
        if(emojiId== -1){
            mPrevEmojiId = -1
            initSlices()
        }
        else{
            val adjs = emojiGraph.getAdj(emojiId)
            for(i in 0 until mSliceNum) {
                if(i < adjs.size){
                    mSlices[i].mEmojiId = adjs[i]
                }
                else
                    mSlices[i].mEmojiId = -1
            }
        }
    }

    internal fun updateCircle(emojiId: Int, faceAnalyzer: FaceAnalyzer){
        this.faceAnalyzer = faceAnalyzer
        mCurrentEmojiId = emojiId
        updateSlices(mCurrentEmojiId)
        invalidate()
    }

    private fun drawCircle(canvas: Canvas){
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 5F
        mPaint.color = Color.WHITE
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadiusScale * circleRadius, mPaint)

        /* draw emoji */
        val emojiSize = if(!mEmojiUpdated) mCircleEmojiScale * 1.2f * (mCircleRadiusScale * mCircleRadiusScale * circleRadius * circleRadius)/(mInnerRadius*2)
        else circleEmojiSize
        drawEmoji(canvas, mCurrentEmojiId, mCenterX, mCenterY, emojiSize)
    }

    private fun drawEmoji(canvas: Canvas, emojiId: Int, centerX: Float, centerY: Float, emojiSize: Float, alpha: Int = 255){
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.alpha = alpha
        val id: Int = if(emojiId != -1)
            context.resources.getIdentifier("zzz_${mPlatform.name.lowercase()}_${emojiId}", "drawable", context.packageName)
        else
            context.resources.getIdentifier("zzz_${mPlatform.name.lowercase()}_no", "drawable", context.packageName)
        val bmp = BitmapFactory.decodeResource(context.resources, id)
        var mRectF =RectF()
        mRectF.left = centerX - emojiSize
        mRectF.right = centerX + emojiSize
        mRectF.top = centerY - emojiSize
        mRectF.bottom = centerY + emojiSize
        canvas.drawBitmap(bmp, null, mRectF, paint)
    }

}
