package com.android.mca2021.keyboard.core

import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.asav.facialprocessing.mtcnn.MTCNNModel
import com.google.mlkit.vision.face.Face
import java.util.*
import android.graphics.BitmapFactory

import android.graphics.Bitmap

import android.content.Context
import android.content.res.AssetManager
import com.android.mca2021.keyboard.core.mtcnn.Box
import com.asav.facialprocessing.mtcnn.MTCNNModel.Companion.create
import java.io.ByteArrayOutputStream
import java.util.Collections.max
import kotlin.math.max
import android.graphics.YuvImage
import android.media.Image
import java.nio.ByteBuffer
import java.util.Collections.min
import kotlin.math.min


internal class FaceAnalyzer(
    context: Context,
    assets: AssetManager,
) : ImageAnalysis.Analyzer {
    private val minFaceSize = 32
    private var mtcnnFaceDetector: MTCNNModel? = null
    private var emotionClassifierTfLite: EmotionTfLiteClassifier? = null

    var listener: Listener? = null

    init {
        try {
            emotionClassifierTfLite = EmotionTfLiteClassifier(context)
        } catch (e: java.lang.Exception) {
            Log.e("FACEOMJI", "Exception initializing EmotionTfLiteClassifier!")
        }
        try {
            mtcnnFaceDetector = create(assets)
        } catch (e: java.lang.Exception) {
            Log.e("FACEOMJI", "Exception initializing MTCNNModel!")
        }
    }

    override fun analyze(imageProxy: ImageProxy) {
        val bitmapImage = imageProxy.toBitmap()!!

        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(-90f)

        val rotated = Bitmap.createBitmap(bitmapImage, 0, 0,
        bitmapImage.width, bitmapImage.height, rotateMatrix, false);
        mtcnnDetectionAndAttributesRecognition(rotated, emotionClassifierTfLite)
//        if (image != null) {
//            val bitmapImage = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
//            yuvToRgbConverter.yuvToRgb(image, bitmapImage)
//        }
        imageProxy.image?.close()
        imageProxy.close()
    }

    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun mtcnnDetectionAndAttributesRecognition(image: Bitmap, classifier: TfLiteClassifier?) {
        val bmp: Bitmap = image
        val bboxes: Vector<Box> = mtcnnFaceDetector!!.detectFaces(
            bmp,
            minFaceSize
        )
        if (bboxes.isEmpty()) {
            listener?.onEmotionDetected("Neutral")
            return
        }
        val box = bboxes.first()
        val bbox: Rect =
            box.transform2Rect() //new android.graphics.Rect(Math.max(0,box.left()),Math.max(0,box.top()),box.right(),box.bottom());
        if (classifier != null && bbox.width() > 0 && bbox.height() > 0) {
            val bboxOrig = Rect(
                bbox.left * bmp.width / bmp.width,
                bmp.height * bbox.top / bmp.height,
                bmp.width * bbox.right / bmp.width,
                bmp.height * bbox.bottom / bmp.height
            )
            val faceBitmap = Bitmap.createBitmap(
                bmp,
                0,
                0,
                bboxOrig.width(),
                bboxOrig.height()
            )
            val resultBitmap = Bitmap.createScaledBitmap(
                faceBitmap,
                classifier.imageSizeX,
                classifier.imageSizeY,
                false
            )
            val res = classifier.classifyFrame(resultBitmap)
            listener?.onEmotionDetected(res.toString())
        }
    }

    // implemented at KeyboardCamera.kt
    internal interface Listener {
        /** Callback that receives face bounds that can be drawn on top of the viewfinder.  */
        fun onFacesDetected(proxyWidth: Int, proxyHeight: Int, face: Face)

        /** Callback that receives face bounds that can be drawn on top of the viewfinder.  */
        fun onEmotionDetected(emotion: String)

        /** Invoked when an error is encounter during face detection.  */
        fun onError(exception: Exception)
    }
}
