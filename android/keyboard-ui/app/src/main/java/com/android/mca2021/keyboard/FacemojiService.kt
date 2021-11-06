package com.android.mca2021.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.mca2021.keyboard.keyboardview.*

class FacemojiService : InputMethodService() {
    lateinit var keyboardView: LinearLayout
    lateinit var keyboardFrame: FrameLayout

    lateinit var currentKeyboard: FacemojiKeyboard

    lateinit var keyboardEnglish: KeyboardEnglish
    lateinit var keyboardKorean: KeyboardKorean
    lateinit var keyboardSymbol: KeyboardSymbol
    lateinit var keyboardEmoji : KeyboardEmoji
    lateinit var keyboardCamera: KeyboardCamera

    private val keyboardInteractionManager = object : KeyboardInteractionManager {
        var currentMode = KeyboardInteractionManager.KeyboardType.ENGLISH

        override fun changeMode(mode: KeyboardInteractionManager.KeyboardType) {
            currentMode = mode
            currentKeyboard = when (currentMode) {
                KeyboardInteractionManager.KeyboardType.ENGLISH -> {
                    keyboardEnglish
                }
                KeyboardInteractionManager.KeyboardType.KOREAN -> {
                    keyboardKorean
                }
                KeyboardInteractionManager.KeyboardType.SYMBOL -> {
                    keyboardSymbol
                }
                KeyboardInteractionManager.KeyboardType.EMOJI -> {
                    keyboardEmoji
                }
                KeyboardInteractionManager.KeyboardType.CAMERA -> {
                    keyboardCamera
                }
            }

            initializeKeyboard()
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        keyboardFrame = keyboardView.findViewById(R.id.keyboard_frame)
        keyboardFrame.minimumHeight = 400
    }

    fun initializeKeyboard() {
        currentInputConnection.finishComposingText()
        keyboardFrame.removeAllViews()
        currentKeyboard.inputConnection = currentInputConnection
        currentKeyboard.initKeyboard()
        keyboardFrame.addView(currentKeyboard.getLayout())
    }

    override fun onCreateInputView(): View {
        keyboardEnglish = KeyboardEnglish(applicationContext, layoutInflater, keyboardInteractionManager)
        keyboardKorean = KeyboardKorean(applicationContext, layoutInflater, keyboardInteractionManager)
        keyboardSymbol = KeyboardSymbol(applicationContext, layoutInflater, keyboardInteractionManager)
        keyboardEmoji = KeyboardEmoji(applicationContext, layoutInflater, keyboardInteractionManager)
        keyboardCamera = KeyboardCamera(this, applicationContext, layoutInflater, keyboardInteractionManager)

        keyboardInteractionManager.changeMode(KeyboardInteractionManager.KeyboardType.ENGLISH)

        initializeKeyboard()

        return keyboardView
    }

    override fun updateInputViewShown() {
        super.updateInputViewShown()
        initializeKeyboard()
    }
}