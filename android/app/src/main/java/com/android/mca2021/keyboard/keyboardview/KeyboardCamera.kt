package com.android.mca2021.keyboard.keyboardview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.os.*
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.mlkit.vision.face.Face
import com.android.mca2021.keyboard.*
import com.android.mca2021.keyboard.MainActivity.Companion.REQUEST_PERMISSION
import com.android.mca2021.keyboard.MainActivity.Companion.REQUIRED_PERMISSIONS
import com.android.mca2021.keyboard.core.FaceAnalyzer
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class KeyboardCamera (
    private val service: FacemojiService,
    override val context: Context,
    private val assets: AssetManager,
    private val layoutInflater: LayoutInflater,
    override val keyboardInteractionListener: KeyboardInteractionManager,
): FacemojiKeyboard(), LifecycleOwner {
    private lateinit var cameraLayout: View

    override var inputConnection: InputConnection? = null
    override var vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cameraExecutor: ExecutorService
    private val TAG: String = "mojiface"

    private var reloadEmotion: Boolean = true

    private var emojiList: List<String>

    /*

    private var emojiItemIds = listOf(
        R.id.recommendation_1,
        R.id.recommendation_2,
        R.id.recommendation_3,
        R.id.recommendation_4
    )
     */

    private val labelEmojis = mapOf(
        "Anger" to "\uD83D\uDE21",
        "Contempt" to "\uD83D\uDE12",
        "Disgust" to "\uD83D\uDE23",
        "Fear" to "\uD83D\uDE28",
        "Happiness" to "\uD83D\uDE42",
        "Neutral" to "\uD83D\uDE10",
        "Sadness" to "\uD83D\uDE1E",
        "Surprise" to "\uD83D\uDE2E",
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

    private val emotionTextIds = arrayOf(
        R.id.anger_text,
        R.id.disgust_text,
        R.id.fear_text,
        R.id.happiness_text,
        R.id.neutral_text,
        R.id.sadness_text,
        R.id.surprise_text,
    )

    /* UI */
    private lateinit var btn_emoji0: Button
    private lateinit var btn_emoji1: Button
    private val openMenuAnim: Animation by lazy {AnimationUtils.loadAnimation(context, R.anim.open_anim)}
    private val closeMenuAnim: Animation by lazy {AnimationUtils.loadAnimation(context, R.anim.close_anim)}
    private var isMenuOpened = false

    override fun changeCaps() {}

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        emojiList = listOf(0x1F600, 0x1F601, 0x1F600, 0x1F600).map{ getEmojiByUnicode(it) }
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setEmojiLayout() {
        /*
        emojiList.forEachIndexed { idx, emoji ->
            val textView = cameraLayout
                .findViewById<View>(emojiItemIds[idx])
                .findViewById<TextView>(R.id.emoji_text)

            textView.text = emoji
            textView.setOnClickListener {
                inputConnection?.commitText((it as TextView).text.toString(), 1)
            }
        }

         */
        /* First emoji0 will be set as follow later */
        // btn_emoji0.text = emojiList[0]
    }

    private fun setEmotionText(scores: FloatArray) {
        scores.forEachIndexed { idx, score ->
            val textView = cameraLayout.findViewById<TextView>(emotionTextIds[idx])
            textView.text = emotions[idx] + ": " + score
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initKeyboard() {
        cameraLayout = layoutInflater.inflate(R.layout.keyboard_camera, null)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val config = context.resources.configuration
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)
        cameraExecutor = Executors.newSingleThreadExecutor()

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

        cameraLayout.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(isMenuOpened){
                        reloadEmotion = true
                        isMenuOpened = false
                        animateGraphButtons(false)
                    }
                }
                MotionEvent.ACTION_UP -> {
                }
                MotionEvent.ACTION_CANCEL -> {
                }
            }
            return@setOnTouchListener true
        }

        /* UI */
        btn_emoji0 = cameraLayout.findViewById(R.id.btn_emoji0)
        btn_emoji1 = cameraLayout.findViewById(R.id.btn_emoji1)

        btn_emoji0.text = "1"

        btn_emoji0.setOnClickListener {
            if(isMenuOpened){ /* use that emoji and close menu */
                Toast.makeText(context, "Used Emoji " + btn_emoji0.text, Toast.LENGTH_SHORT).show()
                isMenuOpened = false
                reloadEmotion = true
                btn_emoji1.isClickable = false
                animateGraphButtons(false)
            } else{ /* open menu */
                isMenuOpened = true
                reloadEmotion = false
                btn_emoji1.isClickable = true
                updateGraphButtons()
            }
        }

        btn_emoji1.setOnClickListener {
            if(isMenuOpened){
                btn_emoji0.text = btn_emoji1.text
                updateGraphButtons()
            }else {
                Toast.makeText(context, "Something is wrong. This cannot be clicked.", Toast.LENGTH_SHORT).show()
            }
        }

        setEmojiLayout()
    }

    private fun updateGraphButtons() {
        btn_emoji1.text = getAdjacentEmojis(btn_emoji0.text as String)
        animateGraphButtons(true)
    }

    private fun animateGraphButtons(isOpening: Boolean) {
        if(isOpening)
            btn_emoji1.startAnimation(openMenuAnim)
        else
            btn_emoji1.startAnimation(closeMenuAnim)
    }

    private fun getAdjacentEmojis(emoji0: String): String {
        /*
        This function will return adjacent emojis of emoji0 (based on emoji graph) later.
        As prototype, just get integer string and return doubled value of it.
         */
        return (emoji0.toInt() * 2).toString()
    }


    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
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

        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(realTimeOpts)

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
                .also{
                    it.setAnalyzer(cameraExecutor, createFaceDetector())
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis, preview)

            } catch(exc: Exception) {
                Log.e("facemoji", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(service))
    }

    override fun getLayout(): View {
        return cameraLayout
    }

    private fun createFaceDetector(): ImageAnalysis.Analyzer {
        val faceDetector = FaceAnalyzer(context, assets)
        faceDetector.listener = object : FaceAnalyzer.Listener {
            override fun onFacesDetected(proxyWidth: Int, proxyHeight: Int, face: Face) {
//                val faceContourOverlay = cameraLayout.findViewById<FaceContourOverlay>(R.id.faceContourOverlay)
//                faceContourOverlay.post { faceContourOverlay.drawFaceBounds(proxyWidth, proxyHeight, face)}
            }

            override fun onEmotionDetected(emotion: String) {
                emojiList = listOf(labelEmojis[emotion]!!) + emojiList
                emojiList = emojiList.subList(0, 4)
                Handler(Looper.getMainLooper()).post {
                    if(reloadEmotion) {
                        setEmojiLayout()
                    }
                }
            }

            override fun onEmotionScoreDetected(scores: FloatArray) {
                Handler(Looper.getMainLooper()).post {
                    if (reloadEmotion) {
                        setEmotionText(scores)
                    }
                }
            }

            override fun onError(exception: Exception) {
                Log.e(TAG, "Face detection error", exception)
            }
        }
        return faceDetector
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}