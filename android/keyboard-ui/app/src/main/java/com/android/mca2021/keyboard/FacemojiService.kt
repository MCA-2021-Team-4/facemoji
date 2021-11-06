package com.android.mca2021.keyboard

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.mca2021.keyboard.keyboardview.*

class FacemojiService : InputMethodService() {
    lateinit var keyboardView: LinearLayout
    lateinit var keyboardFrame: FrameLayout
    lateinit var keyboardEnglish: KeyboardEnglish
    lateinit var keyboardKorean: KeyboardKorean
    lateinit var keyboardSymbol: KeyboardSymbol
    lateinit var keyboardEmoji : KeyboardEmoji
    lateinit var keyboardCamera: KeyboardCamera

    val keyboardInteractionListener = object : KeyboardInteractionListener {
        var currentMode = KeyboardInteractionListener.KeyboardType.ENGLISH

        override fun changeMode(mode: KeyboardInteractionListener.KeyboardType) {
            currentInputConnection.finishComposingText()
            currentMode = mode

            connectInput()
        }

        override fun connectInput() {
            keyboardFrame.removeAllViews()
            when (currentMode) {
                KeyboardInteractionListener.KeyboardType.ENGLISH -> {
                    keyboardEnglish.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardEnglish.getLayout())
                }
                KeyboardInteractionListener.KeyboardType.KOREAN -> {
                    keyboardKorean.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardKorean.getLayout())
                }
                KeyboardInteractionListener.KeyboardType.SYMBOL -> {
                    keyboardSymbol.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardSymbol.getLayout())
                }
                KeyboardInteractionListener.KeyboardType.EMOJI -> {
                    keyboardEmoji.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardEmoji.getLayout())
                }
                KeyboardInteractionListener.KeyboardType.CAMERA -> {
                    keyboardCamera.initKeyboard()
                    keyboardCamera.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardCamera.getLayout())
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        keyboardFrame = keyboardView.findViewById(R.id.keyboard_frame)
        keyboardFrame.minimumHeight = 400
    }

    override fun onCreateInputView(): View {
        keyboardEnglish = KeyboardEnglish(applicationContext, layoutInflater, keyboardInteractionListener)
        keyboardKorean = KeyboardKorean(applicationContext, layoutInflater, keyboardInteractionListener)
        keyboardSymbol = KeyboardSymbol(applicationContext, layoutInflater, keyboardInteractionListener)
        keyboardEmoji = KeyboardEmoji(applicationContext, layoutInflater, keyboardInteractionListener)
        keyboardCamera = KeyboardCamera(this, applicationContext, layoutInflater, keyboardInteractionListener)

        keyboardEnglish.inputConnection = currentInputConnection
        keyboardKorean.inputConnection = currentInputConnection
        keyboardEmoji.inputConnection = currentInputConnection
        keyboardSymbol.inputConnection = currentInputConnection
        keyboardCamera.inputConnection = currentInputConnection

        keyboardKorean.initKeyboard()
        keyboardEnglish.initKeyboard()
        keyboardEmoji.initKeyboard()
        keyboardSymbol.initKeyboard()

        keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.ENGLISH)
        return keyboardView
    }

    override fun updateInputViewShown() {
        super.updateInputViewShown()
        currentInputConnection.finishComposingText()
        keyboardInteractionListener.connectInput()
//        if (currentInputEditorInfo.inputType == EditorInfo.TYPE_CLASS_NUMBER) {
//            keyboardFrame.removeAllViews()
////            keyboardFrame.addView(KeyboardNumpad.newInstance(applicationContext, layoutInflater, currentInputConnection, keyboardInterationListener))
//        } else {
//            keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.ENGLISH)
//        }
    }
}