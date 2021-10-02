package com.android.mca2021.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.android.mca2021.keyboard.keyboardview.*

class KeyBoardService : InputMethodService(){
    lateinit var keyboardView:LinearLayout
    lateinit var keyboardFrame:FrameLayout
    lateinit var keyboardEnglish:KeyboardEnglish

    val keyboardInterationListener = object:KeyboardInterationListener{

        override fun modeChange(mode: Int) {
            currentInputConnection.finishComposingText()
            when(mode){
                0 -> {
                    keyboardFrame.removeAllViews()
                    keyboardEnglish.inputConnection = currentInputConnection
                    keyboardFrame.addView(keyboardEnglish.getLayout())
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
        keyboardEnglish = KeyboardEnglish(applicationContext, layoutInflater, keyboardInterationListener)
        keyboardEnglish.inputConnection = currentInputConnection
        keyboardEnglish.init()
        return keyboardView
    }

    override fun updateInputViewShown() {
        super.updateInputViewShown()
        currentInputConnection.finishComposingText()
        if(currentInputEditorInfo.inputType == EditorInfo.TYPE_CLASS_NUMBER){
            keyboardFrame.removeAllViews()
//            keyboardFrame.addView(KeyboardNumpad.newInstance(applicationContext, layoutInflater, currentInputConnection, keyboardInterationListener))
        }
        else{
            keyboardInterationListener.modeChange(0)
        }
    }
}