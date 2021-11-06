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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent.getBooleanExtra(REQUEST_PERMISSION, false).also {
            if (it) ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onResume() {
        super.onResume()
        checkKeyboardSettings()
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this,
                    R.string.not_permitted,
                    Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    fun enableKeyboard(view: android.view.View) {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    fun openKeyboardSetting(view: android.view.View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showInputMethodPicker()
    }

    fun checkKeyboardSettings() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = inputMethodManager.enabledInputMethodList
        val btn_enablekeyboard = findViewById<Button>(R.id.btn_enablekeyboard)
        var enabled = false
        for(i in 0 until enabledMethods.size){
            val imi = enabledMethods[i]
            val name = imi.loadLabel(packageManager).toString()
            Log.d(TAG, name)
            if(name.equals("Facemoji")){
                enabled = true
                btn_enablekeyboard.setBackgroundColor(Color.BLUE)
                btn_enablekeyboard.setText("facemoji enabled! ✓")
            }
        }
        if(enabled == false){
            btn_enablekeyboard.setBackgroundColor(Color.DKGRAY)
            btn_enablekeyboard.setText("enable facemoji")
        }

        val defaultIME = Settings.Secure.getString(this.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val btn_selectkeyboard = findViewById<Button>(R.id.btn_selectkeyboard)
        if(defaultIME.contains("facemoji", ignoreCase = true)) {
            btn_selectkeyboard.setBackgroundColor(Color.BLUE)
            btn_selectkeyboard.setText("facemoji selected! ✓")
        }else{
            btn_selectkeyboard.setBackgroundColor(Color.DKGRAY)
            btn_selectkeyboard.setText("select facemoji as default")
        }
    }

    fun checkIfDefault(view: android.view.View) {
        val defaultIME = Settings.Secure.getString(this.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val btn_selectkeyboard = findViewById<Button>(R.id.btn_selectkeyboard)
        if(defaultIME.contains("facemoji", ignoreCase = true)) {
            btn_selectkeyboard.setBackgroundColor(Color.BLUE)
            btn_selectkeyboard.setText("facemoji selected! ✓")
            Toast.makeText(this.applicationContext, "facemoji is set as default keyboard", Toast.LENGTH_SHORT).show()
        }else{
            btn_selectkeyboard.setBackgroundColor(Color.DKGRAY)
            btn_selectkeyboard.setText("select facemoji as default")
            Toast.makeText(this.applicationContext, "facemoji not set as default keyboard", Toast.LENGTH_SHORT).show()
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
