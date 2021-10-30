package com.android.mca2021.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.mca2021.keyboard.keyboardview.*

class FacemojiService : InputMethodService() {
    lateinit var keyboardView: LinearLayout
    lateinit var keyboardFrame: FrameLayout
    lateinit var keyboardEnglish: KeyboardEnglish
    lateinit var keyboardKorean: KeyboardKorean
    lateinit var keyboardSimbol: KeyboardSimbol
    lateinit var keyboardCamera: KeyboardCamera

    val keyboardInteractionListener = object : KeyboardInteractionListener {

        override fun changeMode(mode: KeyboardInteractionListener.KeyboardType) {
            currentInputConnection.finishComposingText()
            when (mode) {
                KeyboardInteractionListener.KeyboardType.ENGLISH -> {
                    keyboardFrame.removeAllViews()
                    keyboardEnglish.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardEnglish.getLayout())
                }
                KeyboardInteractionListener.KeyboardType.KOREAN -> {
                    keyboardFrame.removeAllViews()
                    keyboardKorean.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardKorean.getLayout())
                }
                KeyboardInteractionListener.KeyboardType.SYMBOL -> {

                }
                KeyboardInteractionListener.KeyboardType.CAMERA -> {
                    keyboardFrame.removeAllViews()
                    keyboardCamera.inputConnection = currentInputConnection
                    keyboardCamera.initKeyboard()
                    keyboardFrame.addView(keyboardCamera.getLayout())
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        keyboardFrame = keyboardView.findViewById(R.id.keyboard_frame)
    }

    override fun onCreateInputView(): View {
        keyboardEnglish = KeyboardEnglish(applicationContext, layoutInflater, keyboardInteractionListener)
        keyboardKorean = KeyboardKorean(applicationContext, layoutInflater, keyboardInteractionListener)
        keyboardSimbol = KeyboardSimbol(applicationContext, layoutInflater, keyboardInteractionListener)
        keyboardCamera = KeyboardCamera(this, applicationContext, layoutInflater, keyboardInteractionListener)

        keyboardEnglish.inputConnection = currentInputConnection
        keyboardKorean.inputConnection = currentInputConnection
        keyboardSimbol.inputConnection = currentInputConnection
        keyboardKorean.initKeyboard()
        keyboardEnglish.initKeyboard()
        keyboardSimbol.initKeyboard()
        return keyboardView
    }

    override fun updateInputViewShown() {
        super.updateInputViewShown()
        currentInputConnection.finishComposingText()
        if (currentInputEditorInfo.inputType == EditorInfo.TYPE_CLASS_NUMBER) {
            keyboardFrame.removeAllViews()
//            keyboardFrame.addView(KeyboardNumpad.newInstance(applicationContext, layoutInflater, currentInputConnection, keyboardInterationListener))
        } else {
            keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.ENGLISH)
        }
    }
}