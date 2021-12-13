package com.android.mca2021.keyboard

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.android.mca2021.keyboard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getBooleanExtra(REQUEST_PERMISSION, false).also {
            if (it) ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        binding.btnVibration.setOnClickListener {
            binding.vibrationSwitch.toggle()
        }

        binding.vibrationSwitch.isChecked = sharedPreferences.getInt("keyboardVibrate", -1) > 0

        binding.vibrationSwitch.setOnCheckedChangeListener { v, isChecked ->
            sharedPreferences.edit {
                this.putInt("keyboardVibrate", if (isChecked) 1 else -1)
                this.apply()
                this.commit()
            }
        }

        binding.btnEmojiPlatform.setOnClickListener {
            openPlatformDialog()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) checkKeyboardSettings()
    }

    override fun onResume() {
        super.onResume()
        checkKeyboardSettings()
    }

    private fun openPlatformDialog() {
        val platformList = EmojiPlatform.all.map { it.name.lowercase() }.toTypedArray()
        val selectedPlatform = sharedPreferences.getString("emojiPlatform", "google")!!
        val selectedIndex = platformList.indexOf(selectedPlatform)
        AlertDialog.Builder(this).setSingleChoiceItems(
            platformList,
            selectedIndex
        ) { dialog, which ->
            sharedPreferences.edit {
                this.putString("emojiPlatform", platformList[which])
                this.apply()
                this.commit()
            }
        }
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    R.string.not_permitted,
                    Toast.LENGTH_SHORT
                ).show()
            }
            finish()
        }
    }

    fun enableKeyboard(view: View) {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    fun openKeyboardSetting(view: View) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showInputMethodPicker()
    }

    private fun checkKeyboardSettings() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = inputMethodManager.enabledInputMethodList
        val enabled = enabledMethods.last {
            it.loadLabel(packageManager).toString().equals("Facemoji")
        } != null
        if (!enabled) {
            binding.defaultIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            binding.defaultIndicator.text = resources.getString(R.string.keyboard_not_added)
            return
        }

        val defaultIME =
            Settings.Secure.getString(this.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        if (defaultIME.contains("facemoji", ignoreCase = true)) {
            binding.defaultIndicator.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            binding.defaultIndicator.text = resources.getString(R.string.set_as_default)
        } else {
            binding.defaultIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            binding.defaultIndicator.text = resources.getString(R.string.not_set_as_default)
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val REQUEST_PERMISSION = "com.android.mca2021.keyboard.requestPermission"
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
