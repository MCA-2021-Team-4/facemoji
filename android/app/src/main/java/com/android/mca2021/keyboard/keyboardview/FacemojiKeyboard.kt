package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.media.AudioManager
import android.os.*
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.android.mca2021.keyboard.KeyboardInteractionManager
import com.android.mca2021.keyboard.R

abstract class FacemojiKeyboard {
    abstract val context: Context
    abstract val vibrator: Vibrator
    abstract var inputConnection: InputConnection?
    abstract val keyboardInteractionListener: KeyboardInteractionManager


    open val myKeysText: List<List<String>> = listOf()
    open val myLongClickKeysText: List<List<String>> = listOf()
    open var layoutLines: List<LinearLayout> = listOf()

    var buttons: MutableList<View> = mutableListOf()
    var isCaps: Boolean = false
    var sound = 0
    var vibrate = 0

    abstract fun initKeyboard()

    abstract fun getLayout(): View

    fun getOnTouchListener(clickListener: View.OnClickListener): View.OnTouchListener {
        val handler = Handler()
        val initailInterval = 500
        val normalInterval = 10
        lateinit var clickedView: View
        val handlerRunnable = object : Runnable {
            override fun run() {
                handler.postDelayed(this, normalInterval.toLong())
                clickListener.onClick(clickedView)
            }
        }

        return object : View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent?): Boolean {
                clickedView = view
                when (motionEvent?.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        handler.removeCallbacks(handlerRunnable)
                        handler.postDelayed(handlerRunnable, initailInterval.toLong())
                        view.isPressed = true
                        clickListener.onClick(view)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacks(handlerRunnable)
                        view.isPressed = false
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        handler.removeCallbacks(handlerRunnable)
                        return true
                    }
                }
                return false
            }
        }
    }

    fun getMyLongClickListener(textView: TextView): View.OnLongClickListener {
        return object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    inputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
                }
                playVibrate()
                when (textView.text.toString()) {
                    "한/영" -> {
                        keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.KOREAN)
                    }
                    "!#1" -> {
                        keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.SYMBOL)
                    }
                    else -> {
                        playClick(textView.text.toString().toCharArray().get(0).toInt())
                        inputConnection?.commitText(textView.text.toString(), 1)
                    }
                }
                return true
            }
        }
    }

    fun getMyClickListener(actionButton: View): View.OnClickListener {
        val clickListener = (View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                inputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
            }
            val textView = actionButton.findViewById<TextView>(R.id.key_text)
            playVibrate()
            val cursorcs: CharSequence? =
                inputConnection?.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
            if (cursorcs != null && cursorcs.length >= 2) {
                Log.d("JIHO", "test log : $cursorcs")
                val eventTime = SystemClock.uptimeMillis()
                inputConnection?.finishComposingText()
                inputConnection?.sendKeyEvent(
                    KeyEvent(
                        eventTime, eventTime,
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD
                    )
                )
                inputConnection?.sendKeyEvent(
                    KeyEvent(
                        SystemClock.uptimeMillis(), eventTime,
                        KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD
                    )
                )
                playClick(
                    textView.text.toString().toCharArray().get(
                        0
                    ).toInt()
                )
                inputConnection?.commitText(textView.text, 1)
            } else {
                when (textView.text.toString()) {
                    "\uD83D\uDE00" -> {
                        keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.EMOJI)
                    }
                    "가" -> {
                        keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.KOREAN)
                    }
                    "한/영" -> {
                        changeLanguage()
                    }
                    "!#1" -> {
                        keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.SYMBOL)
                    }
                    else -> {
                        handleTextButton(actionButton)
                    }
                }
            }
        })
        actionButton.setOnClickListener(clickListener)
        return clickListener
    }

    open fun changeLanguage() {
        keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.KOREAN)
    }

    open fun handleTextButton(actionButton: View) {
        val textView = actionButton.findViewById<TextView>(R.id.key_text)
        playClick(
            textView.text.toString().toCharArray().get(
                0
            ).code
        )
        inputConnection?.commitText(textView.text, 1)
    }

    open fun setLayoutComponents() {
        for (line in layoutLines.indices) {
            val children = layoutLines[line].children.toList()
            val myText = myKeysText[line]
            for (item in children.indices) {
                val actionButton = children[item].findViewById<View>(R.id.key_button)
                val textView = actionButton.findViewById<TextView>(R.id.key_text)
                val specialKey = children[item].findViewById<ImageView>(R.id.special_key)
                val resourceId = when (myText[item]) {
                    "SPACE" -> R.drawable.ic_space_bar
                    "DEL" -> R.drawable.backspace_dark
                    "CAPS" -> R.drawable.ic_caps_unlock
                    "ENTER" -> R.drawable.ic_enter
                    "CAM" -> R.drawable.cam
                    else -> 0
                }
                val myOnClickListener = when (myText[item]) {
                    "SPACE" -> getSpaceAction()
                    "DEL" -> getDeleteAction()
                    "CAPS" -> getCapsAction()
                    "ENTER" -> getEnterAction()
                    "CAM" -> getCamAction()
                    else -> {
                        textView.text = myText[item]
                        buttons.add(actionButton)
                        getMyClickListener(actionButton)
                    }
                }
                val myOnTouchListener: View.OnTouchListener? = when(myText[item]) {
                    "DEL" -> getOnTouchListener(myOnClickListener)
                    else -> null
                }
                if (resourceId != 0) {
                    specialKey.setImageResource(resourceId)
                    specialKey.visibility = View.VISIBLE
                    actionButton.visibility = View.GONE
                    specialKey.setOnClickListener(myOnClickListener)
                    if(myOnTouchListener != null) {
                        specialKey.setOnTouchListener(myOnTouchListener)
                    }
                    specialKey.setBackgroundResource(R.drawable.key_background)
                }
//                children[item].setOnTouchListener(getOnTouchListener(myOnClickListener))
            }
        }
    }

    fun playClick(i: Int) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        when (i) {
            32 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            else -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    open fun playVibrate() {
        if (vibrate > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10, 100))
            } else {
                vibrator.vibrate(10)
            }
        }
    }

    fun getCamAction(): View.OnClickListener {
        return View.OnClickListener {
            playVibrate()
            keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.CAMERA)
        }
    }

    open fun getSpaceAction(): View.OnClickListener {
        return View.OnClickListener {
            playClick('ㅂ'.toInt())
            playVibrate()
            inputConnection?.commitText(" ", 1)
        }
    }

    open fun getDeleteAction(): View.OnClickListener {
        return View.OnClickListener {
            playVibrate()
            val eventTime = SystemClock.uptimeMillis()
            inputConnection?.finishComposingText()
            inputConnection?.sendKeyEvent(
                KeyEvent(
                    eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD
                )
            )
            inputConnection?.sendKeyEvent(
                KeyEvent(
                    SystemClock.uptimeMillis(), eventTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD
                )
            )
        }
    }

    open fun getCapsAction(): View.OnClickListener {
        return View.OnClickListener {
            playVibrate()
            changeCaps()
        }
    }

    abstract fun changeCaps()

    open fun getEnterAction(): View.OnClickListener {
        return View.OnClickListener {
            playVibrate()
            val eventTime = SystemClock.uptimeMillis()
            inputConnection?.sendKeyEvent(
                KeyEvent(
                    eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD
                )
            )
            inputConnection?.sendKeyEvent(
                KeyEvent(
                    SystemClock.uptimeMillis(), eventTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD
                )
            )
        }
    }
}