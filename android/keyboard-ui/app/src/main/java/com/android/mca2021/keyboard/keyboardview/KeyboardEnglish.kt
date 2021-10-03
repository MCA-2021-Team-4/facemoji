package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.media.AudioManager
import android.content.Context.AUDIO_SERVICE
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import com.android.mca2021.keyboard.*
import java.util.*

class KeyboardEnglish constructor(
    var context: Context,
    var layoutInflater: LayoutInflater,
    var keyboardInteractionListener: KeyboardInterationListener
) {
    private lateinit var englishLayout: LinearLayout
    var inputConnection: InputConnection? = null
    private lateinit var vibrator: Vibrator
    private lateinit var sharedPreferences: SharedPreferences

    var isCaps: Boolean = false
    var buttons: MutableList<Button> = mutableListOf<Button>()

    private val numPadText = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val firstLineText = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    private val secondLineText = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    private val thirdLineText = listOf("CAPS", "z", "x", "c", "v", "b", "n", "m", "DEL")
    private val fourthLineText = listOf("!#1", "한/영", ",", "SPACE", ".", "ENTER")

    private val firstLongClickText = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")
    private val secondLongClickText = listOf("~", "+", "-", "×", "♥", ":", ";", "'", "\"")
    private val thirdLongClickText = listOf("∞", "_", "<", ">", "/", ",", "?")

    private val myKeysText = listOf(
        numPadText,
        firstLineText,
        secondLineText,
        thirdLineText,
        fourthLineText,
    )

    private val myLongClickKeysText = listOf(
        firstLongClickText,
        secondLongClickText,
        thirdLongClickText,
    )

    private lateinit var layoutLines: List<LinearLayout>

    var downView: View? = null
    var sound = 0
    var vibrate = 0
    var capsView: ImageView? = null

    fun init() {
        englishLayout = layoutInflater.inflate(R.layout.keyboard_action, null) as LinearLayout
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val numPadLine: LinearLayout = englishLayout.findViewById(R.id.numpad_line)
        val firstLine: LinearLayout = englishLayout.findViewById(R.id.first_line)
        val secondLine: LinearLayout = englishLayout.findViewById(R.id.second_line)
        val thirdLine: LinearLayout = englishLayout.findViewById(R.id.third_line)
        val fourthLine: LinearLayout = englishLayout.findViewById(R.id.fourth_line)

        val config = context.resources.configuration
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        val preferredHeight = sharedPreferences.getInt("keyboardHeight", 150)
        val height =
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) (preferredHeight * 0.7).toInt()
            else preferredHeight

        firstLine.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        secondLine.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        thirdLine.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)

        layoutLines = listOf(
            numPadLine,
            firstLine,
            secondLine,
            thirdLine,
            fourthLine,
        )

        setLayoutComponents()
    }

    fun getLayout(): LinearLayout {
        return englishLayout
    }


    fun modeChange() {
        isCaps = !isCaps
        buttons.forEach {
            it.text =
                if (isCaps) it.text.toString().toLowerCase(Locale.US)
                else it.text.toString().toUpperCase(Locale.US)
        }
    }

    private fun playClick(i: Int) {
        val am = context.getSystemService(AUDIO_SERVICE) as AudioManager?
        when (i) {
            32 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            else -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1.toFloat())
        }
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

    private fun getMyLongClickListener(textView: TextView): View.OnLongClickListener {
        return object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    inputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
                }
                playVibrate()
                val cursorcs: CharSequence? =
                    inputConnection?.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
                if (cursorcs != null && cursorcs.length >= 2) {

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
                when (textView.text.toString()) {
                    "한/영" -> {
                        keyboardInteractionListener.modeChange(1)
                    }
                    "!#1" -> {
                        keyboardInteractionListener.modeChange(2)
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

    private fun getMyClickListener(actionButton: Button): View.OnClickListener {
        val clickListener = (View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                inputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
            }
            playVibrate()
            val cursorcs: CharSequence? =
                inputConnection?.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
            if (cursorcs != null && cursorcs.length >= 2) {
                Log.d("JIHO","test log : $cursorcs")
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
                    actionButton.text.toString().toCharArray().get(
                        0
                    ).toInt()
                )
                inputConnection?.commitText(actionButton.text, 1)
            } else {
                when (actionButton.text.toString()) {

                    "한/영" -> {
                        keyboardInteractionListener.modeChange(1)
                    }
                    "!#1" -> {
                        keyboardInteractionListener.modeChange(2)
                    }
                    else -> {
                        playClick(
                            actionButton.text.toString().toCharArray().get(
                                0
                            ).toInt()
                        )
                        inputConnection?.commitText(actionButton.text, 1)
                    }
                }
            }
        })
        actionButton.setOnClickListener(clickListener)
        return clickListener
    }

    private fun setLayoutComponents() {
        layoutLines.forEachIndexed { line, layout ->
            val children = layout.children.toList()
            val myText = myKeysText[line]
            var longClickIndex = 0
            children.forEachIndexed { item, child ->
                val actionButton = child.findViewById<Button>(R.id.key_button)
                val specialKey = child.findViewById<ImageView>(R.id.special_key)
                val resourceId = when (myText[item]) {
                    "SPACE" -> R.drawable.ic_space_bar
                    "DEL" -> R.drawable.del
                    "CAPS" -> R.drawable.ic_caps_unlock
                    "ENTER" -> R.drawable.ic_enter
                    else -> 0
                }
                val onClickListener = when (myText[item]) {
                    "SPACE" -> getSpaceAction()
                    "DEL" -> getDeleteAction()
                    "CAPS" -> getCapsAction()
                    "ENTER" -> getEnterAction()
                    else -> {
                        actionButton.text = myText[item]
                        buttons.add(actionButton)
                        getMyClickListener(actionButton)
                    }
                }
                if (resourceId == 0) {
                    val longClickTextView =
                        child.findViewById<TextView>(R.id.text_long_click)
                    if (line in 1..3) {//특수기호가 삽입될 수 있는 라인
                        longClickTextView.text = myLongClickKeysText[line - 1][longClickIndex++]
                        longClickTextView.bringToFront()
                        longClickTextView.setOnClickListener(onClickListener)
                        actionButton.setOnLongClickListener(
                            getMyLongClickListener(
                                longClickTextView
                            )
                        )
                        longClickTextView.setOnLongClickListener(
                            getMyLongClickListener(
                                longClickTextView
                            )
                        )
                    }
                } else {
                    specialKey.setImageResource(resourceId)
                    specialKey.visibility = View.VISIBLE
                    actionButton.visibility = View.GONE
                    specialKey.setOnClickListener(onClickListener)
                    specialKey.setOnTouchListener(getOnTouchListener(onClickListener))
                    specialKey.setBackgroundResource(R.drawable.key_background)
                }
                child.setOnClickListener(onClickListener)
            }
        }
    }

    fun getOnTouchListener(clickListener: View.OnClickListener):View.OnTouchListener{
        val handler = Handler()
        val initailInterval = 500
        val normalInterval = 100
        lateinit var clickedView: View
        val handlerRunnable = object: Runnable{
            override fun run() {
                handler.postDelayed(this, normalInterval.toLong())
                clickListener.onClick(clickedView)
            }
        }
        val onTouchListener = object:View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent?): Boolean {
                clickedView = view
                when (motionEvent?.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        handler.removeCallbacks(handlerRunnable)
                        handler.postDelayed(handlerRunnable, initailInterval.toLong())
                        clickListener.onClick(view)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacks(handlerRunnable)
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

        return onTouchListener
    }

    fun getSpaceAction(): View.OnClickListener {
        return View.OnClickListener {
            playClick('ㅂ'.toInt())
            playVibrate()
            inputConnection?.commitText(" ", 1)
        }
    }

    fun getDeleteAction(): View.OnClickListener {
        return View.OnClickListener {
            playVibrate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                inputConnection?.deleteSurroundingTextInCodePoints(1, 0)
            } else {
                inputConnection?.deleteSurroundingText(1, 0)
            }
        }
    }

    fun getCapsAction(): View.OnClickListener {
        return View.OnClickListener {
            playVibrate()
            modeChange()
        }
    }

    fun getEnterAction(): View.OnClickListener {
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