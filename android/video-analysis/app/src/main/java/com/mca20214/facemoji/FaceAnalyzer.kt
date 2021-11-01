package com.mca20214.facemoji

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

internal class FaceAnalyzer() : ImageAnalysis.Analyzer {

  var listener: Listener? = null
  override fun analyze(imageProxy: ImageProxy) {
    val mediaImage = imageProxy.image
    if(mediaImage != null){
      val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

      // Real-time contour detection
      val realTimeOpts = FaceDetectorOptions.Builder()
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
      val detector = FaceDetection.getClient(realTimeOpts)
      val result = detector.process(image)
        .addOnSuccessListener { faces ->
          val listener = listener ?: return@addOnSuccessListener
          for (face in faces) {

            val bounds = face.boundingBox
            /*
            public static final int FACE = 1;
            public static final int LEFT_EYEBROW_TOP = 2;
            public static final int LEFT_EYEBROW_BOTTOM = 3;
            public static final int RIGHT_EYEBROW_TOP = 4;
            public static final int RIGHT_EYEBROW_BOTTOM = 5;
            public static final int LEFT_EYE = 6;
            public static final int RIGHT_EYE = 7;
            public static final int UPPER_LIP_TOP = 8;
            public static final int UPPER_LIP_BOTTOM = 9;
            public static final int LOWER_LIP_TOP = 10;
            public static final int LOWER_LIP_BOTTOM = 11;
            public static final int NOSE_BRIDGE = 12;
            public static final int NOSE_BOTTOM = 13;
            public static final int LEFT_CHEEK = 14;
            public static final int RIGHT_CHEEK = 15;
             */


            val ctr = face.allContours
            listener.onFacesDetected(imageProxy.width, imageProxy.height, face)

            /*
            // not all contours are guaranteed (ex. when only half face)
            Log.d(MainActivity.TAG,"nose point0: " + ctr[12]?.points?.get(0)) // List<PointF>
            Log.d(MainActivity.TAG,"nose point1: " + ctr[12]?.points?.get(1)) // List<PointF>
            Log.d(MainActivity.TAG,"nose point2: " + ctr[12]?.points?.get(2)) // List<PointF>

            // x : right is smaller
            // y : top is smaller


            if(face.smilingProbability != null) {
              val smileProb = face.smilingProbability
              Log.d(MainActivity.TAG, "smileProb: " + smileProb)
            }
            if(face.leftEyeOpenProbability != null) {
              val leftEyeOpenProb = face.leftEyeOpenProbability
              Log.d(MainActivity.TAG, "leftEyeProb: " + leftEyeOpenProb) // currently right eye
            }
            if(face.rightEyeOpenProbability != null) {
              val rightEyeOpenProb = face.rightEyeOpenProbability
              Log.d(MainActivity.TAG, "rightEyeProb: " + rightEyeOpenProb) // currently left eye
            }
            */
          }
        }
        .addOnFailureListener { e ->
          listener?.onError(e)
        }
        .addOnCompleteListener {
          mediaImage.close()
          imageProxy.close()
        }
    }
  }


  internal interface Listener {
    /** Callback that receives face bounds that can be drawn on top of the viewfinder.  */
    fun onFacesDetected(proxyWidth: Int, proxyHeight: Int, face: Face)

    /** Invoked when an error is encounter during face detection.  */
    fun onError(exception: Exception)
  }
}