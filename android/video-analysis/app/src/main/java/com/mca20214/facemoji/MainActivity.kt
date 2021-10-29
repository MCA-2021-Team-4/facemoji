package com.mca20214.facemoji

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit
class MainActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if(allPermissionsGranted())
            startCamera()
        else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

        camera_capture_button.setOnClickListener{ takePhoto() }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()


    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            /*
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }
             */
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FaceAnalyzer)
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    object FaceAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            Log.d(TAG, "in faceanalyzer");
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

                            // not all contours are guaranteed (ex. when only half face)
                            Log.d(TAG,"nose point0: " + ctr[12]?.points?.get(0)) // List<PointF>
                            Log.d(TAG,"nose point1: " + ctr[12]?.points?.get(1)) // List<PointF>
                            Log.d(TAG,"nose point2: " + ctr[12]?.points?.get(2)) // List<PointF>

                            // x : right is smaller
                            // y : top is smaller


                            if(face.smilingProbability != null) {
                                val smileProb = face.smilingProbability
                                Log.d(TAG, "smileProb: " + smileProb)
                            }
                            if(face.leftEyeOpenProbability != null) {
                                val leftEyeOpenProb = face.leftEyeOpenProbability
                                Log.d(TAG, "leftEyeProb: " + leftEyeOpenProb) // currently right eye
                            }
                            if(face.rightEyeOpenProbability != null) {
                                val rightEyeOpenProb = face.rightEyeOpenProbability
                                Log.d(TAG, "rightEyeProb: " + rightEyeOpenProb) // currently left eye
                            }

                        }
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "failed ...." + e)
                    }
                    .addOnCompleteListener {
                        mediaImage.close()
                        imageProxy.close()
                    }
            }
        }
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }


    companion object {
        private const val TAG = "faceface"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}