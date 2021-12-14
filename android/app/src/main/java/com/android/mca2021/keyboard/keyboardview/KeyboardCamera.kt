package com.android.mca2021.keyboard.keyboardview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
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
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.android.mca2021.keyboard.*
import com.android.mca2021.keyboard.MainActivity.Companion.REQUEST_PERMISSION
import com.android.mca2021.keyboard.MainActivity.Companion.REQUIRED_PERMISSIONS
import com.android.mca2021.keyboard.core.FaceAnalyzer
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.mlkit.vision.face.Face
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class KeyboardCamera(
    private val service: FacemojiService,
    override val context: Context,
    private val assets: AssetManager,
    private val layoutInflater: LayoutInflater,
    override val keyboardInteractionListener: KeyboardInteractionManager,
) : FacemojiKeyboard(), LifecycleOwner {
    private lateinit var cameraLayout: View

    override var inputConnection: InputConnection? = null
    override var vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cameraExecutor: ExecutorService
    private val TAG: String = "mojiface"

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

    /* UI */
    private lateinit var pieMenu: PieMenu

    override fun changeCaps() {}
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val faceAnalyzer: FaceAnalyzer by lazy {
        createFaceAnalyzer()
    }

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initKeyboard() {
        cameraLayout = layoutInflater.inflate(R.layout.keyboard_camera, null)
        pieMenu = cameraLayout.findViewById(R.id.pie_menu)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val config = context.resources.configuration
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        cameraExecutor = Executors.newSingleThreadExecutor()
        pieMenu.mPlatform = EmojiPlatform.from(sharedPreferences.getString("emojiPlatform", "google")!!)

        if (allPermissionsGranted()) {
            startCamera(config)
        } else {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                putExtra(REQUEST_PERMISSION, true)
            }
            context.startActivity(intent)
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        val changeModeButton = cameraLayout.findViewById<Button>(R.id.change_camera_input_mode)
        changeModeButton.setOnClickListener {
            cameraExecutor.shutdown()
            keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.ENGLISH)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
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
                    this, cameraSelector, imageAnalysis, preview
                )

            } catch (exc: Exception) {
                Log.e("facemoji", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(service))
    }

    override fun getLayout(): View {
        return cameraLayout
    }

    private fun createFaceAnalyzer(): FaceAnalyzer {
        val faceAnalyzer = FaceAnalyzer(context, assets)
        faceAnalyzer.listener = object : FaceAnalyzer.Listener {
            override fun onFacesDetected(proxyWidth: Int, proxyHeight: Int, face: Face) {}

            override fun onEmotionDetected(emotion: String) {
                Handler(Looper.getMainLooper()).post {
                    pieMenu.updateCircle(labelEmojis[emotion]!!, faceAnalyzer)
                }
            }

            override fun onEmotionScoreDetected(scores: FloatArray) {}

            override fun onError(exception: Exception) {
                Log.e(TAG, "Face detection error", exception)
            }
        }
        return faceAnalyzer
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}