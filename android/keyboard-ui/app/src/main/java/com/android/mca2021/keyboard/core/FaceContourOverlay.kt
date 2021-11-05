package com.android.mca2021.keyboard.core

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import kotlin.math.ceil

/** Overlay where face bounds are drawn.  */
class FaceContourOverlay constructor(context: Context?, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    private var face: Face? = null
    private var proxyWidth: Int = 0
    private var proxyHeight: Int = 0

    private val TAG: String= "mojiface"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // face
        drawFace(canvas, face, FaceContour.FACE, Color.DKGRAY)

        // left <-> right mirrored
        // left eye
        drawFace(canvas, face, FaceContour.LEFT_EYEBROW_TOP, Color.BLUE)
        drawFace(canvas, face, FaceContour.LEFT_EYEBROW_BOTTOM, Color.GREEN)
        val leftEyeOpened = face?.leftEyeOpenProbability
        val leftEyeColor = if(leftEyeOpened != null && leftEyeOpened > 0.4) Color.CYAN else Color.BLACK
        drawFace(canvas, face, FaceContour.LEFT_EYE, leftEyeColor)

        // right eye
        drawFace(canvas, face, FaceContour.RIGHT_EYEBROW_TOP, Color.BLUE)
        drawFace(canvas, face, FaceContour.RIGHT_EYEBROW_BOTTOM, Color.GREEN)
        val rightEyeOpened = face?.rightEyeOpenProbability
        val rightEyeColor = if(rightEyeOpened != null && rightEyeOpened > 0.4) Color.CYAN else Color.BLACK
        drawFace(canvas, face, FaceContour.RIGHT_EYE, rightEyeColor)

        // nose
        drawFace(canvas, face, FaceContour.NOSE_BOTTOM, Color.MAGENTA)
        drawFace(canvas, face, FaceContour.NOSE_BRIDGE, Color.MAGENTA)

        // lip
        val isSmiling = face?.smilingProbability
        val lipColor = if(isSmiling != null && isSmiling > 0.6) Color.RED else Color.YELLOW
        drawFace(canvas, face, FaceContour.LOWER_LIP_BOTTOM, lipColor)
        drawFace(canvas, face, FaceContour.LOWER_LIP_TOP, Color.WHITE)
        drawFace(canvas, face, FaceContour.UPPER_LIP_BOTTOM, Color.WHITE)
        drawFace(canvas, face, FaceContour.UPPER_LIP_TOP, lipColor)

    }


    fun drawFaceBounds(proxyWidth: Int, proxyHeight: Int, face: Face) {
        this.face = face
        this.proxyWidth = proxyWidth
        this.proxyHeight = proxyHeight
        invalidate()
    }

    private fun drawFace(
        canvas: Canvas,
        face: Face?,
        facePosition: Int,
        @ColorInt selectedColor: Int
    ) {
        val contour = face?.getContour(facePosition)
        val path = Path()

        contour?.points?.forEachIndexed { index, pointF ->
            if (index == 0) {
                path.moveTo(
                    translateX(pointF.x),
                    translateY(pointF.y)
                )
            }
            path.lineTo(
                translateX(pointF.x),
                translateY(pointF.y)
            )
        }
        val paint = Paint().apply {
            color = selectedColor
            style = Paint.Style.STROKE
            strokeWidth = 5.0f
        }
        canvas.drawPath(path, paint)
    }
		
		//functions to transform coordinate
    private fun translateX(horizontal: Float): Float {
        val scale1 = width.toFloat() / this.proxyHeight
        val scale2 = height.toFloat() / this.proxyWidth
        val scale = scale1.coerceAtLeast(scale2)
        val offsetX = (width.toFloat() - ceil(this.proxyHeight * scale)) / 2.0f
        val centerX = width.toFloat() / 2
        return centerX - ((horizontal * scale) + offsetX - centerX)
    }

    private fun translateY(vertical: Float): Float {
        val scale1 = width.toFloat() / this.proxyHeight
        val scale2 = height.toFloat() / this.proxyWidth
        val scale = scale1.coerceAtLeast(scale2)
        val offsetY = (height.toFloat() - ceil(this.proxyWidth * scale)) / 2.0f
        return vertical * scale + offsetY
    }
}
