package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.view.LayoutInflater
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.*
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.camera.core.CameraSelector
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class KeyboardCamera (
    private var service: FacemojiService,
    private var context: Context,
    private var layoutInflater: LayoutInflater,
    private var keyboardInteractionListener: KeyboardInteractionListener
): LifecycleOwner {
    private lateinit var cameraLayout: View
    var inputConnection: InputConnection? = null
    private lateinit var vibrator: Vibrator
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cameraExecutor: ExecutorService

    private val lifecycleRegistry = LifecycleRegistry(this)

    var sound = 0
    var vibrate = 0

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun initKeyboard() {
        cameraLayout = layoutInflater.inflate(R.layout.keyboard_camera, null)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val config = context.resources.configuration
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        if (allPermissionsGranted()) {
            startCamera(config)
        } else {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                putExtra(REQUEST_PERMISSION, true)
            }
            context.startActivity(intent)
        }

        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        cameraExecutor = Executors.newSingleThreadExecutor()

        val changeModeButton = cameraLayout.findViewById<Button>(R.id.change_camera_input_mode)
        changeModeButton.setOnClickListener {
            cameraExecutor.shutdown()
            keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.ENGLISH)
        }
    }

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

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Log.e("facemoji", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(service))
    }

    fun getLayout(): View {
        return cameraLayout
    }

    private fun playVibrate() {
        if (vibrate > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(70, vibrate))
            } else {
                vibrator.vibrate(70)
            }
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}