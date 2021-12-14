package com.android.mca2021.keyboard.keyboardview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.android.mca2021.keyboard.*
import com.android.mca2021.keyboard.MainActivity.Companion.REQUEST_PERMISSION
import com.android.mca2021.keyboard.MainActivity.Companion.REQUIRED_PERMISSIONS
import com.android.mca2021.keyboard.core.FaceAnalyzer
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.mlkit.vision.face.Face
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.android.mca2021.keyboard.PieMenu


class WeightSetCamera(
    private val service: AppCompatActivity,
    private val context: Context,
    private val assets: AssetManager,
    private val layoutInflater: LayoutInflater,
){

    private lateinit var cameraLayout: View

    private var vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cameraExecutor: ExecutorService
    private var mPlatform: EmojiPlatform = EmojiPlatform.GOOGLE
    var sound = 0
    var vibrate = 0
    private val TAG: String = "PiMenuCamera"


    private val labelEmojis = mapOf(
        "Anger" to 0,
        "Disgust" to 6,
        "Fear" to 37,
        "Happiness" to 45,
        "Neutral" to 58,
        "Sadness" to 9,
        "Surprise" to 3,
        "Noface" to -1
    )

    private val emotions = arrayOf(
        "Anger",
        "Disgust",
        "Fear",
        "Happiness",
        "Neutral",
        "Sadness",
        "Surprise",
    )

    /* UI */
    private lateinit var imageView: ImageView

    private val faceAnalyzer: FaceAnalyzer by lazy {
        createFaceAnalyzer()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun finishCamera(){
        cameraExecutor.shutdown()
    }
    @SuppressLint("ClickableViewAccessibility")
    fun initCamera() {
        cameraLayout = layoutInflater.inflate(R.layout.weight_camera, null)
        //pieMenu = cameraLayout.findViewById(R.id.circular_button)
        imageView = cameraLayout.findViewById(R.id.image_view)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val config = context.resources.configuration
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        cameraExecutor = Executors.newSingleThreadExecutor()
        mPlatform = EmojiPlatform.from(sharedPreferences.getString("emojiPlatform", "google")!!)
        //pieMenu.mPlatform = EmojiPlatform.from(sharedPreferences.getString("emojiPlatform", "google")!!)

        if (allPermissionsGranted()) {
            startCamera(config)
        } else {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                putExtra(REQUEST_PERMISSION, true)
            }
            context.startActivity(intent)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera(config: Configuration) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(service)
        val viewFinder = cameraLayout.findViewById<PreviewView>(R.id.view_finder)

        val preferredHeight = sharedPreferences.getFloat("cameraHeight", 350f)
        val heightInDp =
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) preferredHeight
            else preferredHeight
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightInDp,
            context.resources.displayMetrics
        ).toInt()
        viewFinder.layoutParams.height = height

        //Face Detect option
        val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowManager.currentWindowMetrics.bounds.width()
            } else {
                windowManager.defaultDisplay.width
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(width, height))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(Surface.ROTATION_90)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, faceAnalyzer)
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    service, cameraSelector, imageAnalysis, preview
                )

            } catch (exc: Exception) {
                Log.e("facemoji", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(service))
    }

    fun getLayout(): View {
        return cameraLayout
    }

    private fun createFaceAnalyzer(): FaceAnalyzer {
        val faceAnalyzer = FaceAnalyzer(context, assets)
        faceAnalyzer.listener = object : FaceAnalyzer.Listener {
            override fun onFacesDetected(proxyWidth: Int, proxyHeight: Int, face: Face) {
            }

            override fun onEmotionDetected(emotion: String) {
                Handler(Looper.getMainLooper()).post {
                    //pieMenu.updateCircle(labelEmojis[emotion]!!, faceAnalyzer)
                    val emojiId = labelEmojis[emotion]
                    var id: Int = if(emojiId == -1)
                        context.resources.getIdentifier("zzz_${mPlatform.name.lowercase()}_no", "drawable", context.packageName)
                    else
                        context.resources.getIdentifier("zzz_${mPlatform.name.lowercase()}_${labelEmojis[emotion]}", "drawable", context.packageName)
                    val bmp = BitmapFactory.decodeResource(context.resources, id)
                    imageView.setImageBitmap(bmp)
                    imageView.alpha = 0.5f
                }
            }

            override fun onEmotionScoreDetected(scores: FloatArray) {
//                Handler(Looper.getMainLooper()).post {
//                    setEmotionText(scores)
//                }
            }

            override fun onError(exception: Exception) {
                Log.e(TAG, "Face detection error", exception)
            }
        }
        return faceAnalyzer
    }
}