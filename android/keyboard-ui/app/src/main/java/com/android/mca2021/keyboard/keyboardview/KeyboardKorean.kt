package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import android.media.AudioManager
import android.os.*
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.android.mca2021.keyboard.*
import java.lang.NumberFormatException

class KeyboardKorean constructor(
    var context: Context,
    var layoutInflater: LayoutInflater,
    var keyboardInteractionListener: KeyboardInteractionListener
) {

    lateinit var koreanLayout: LinearLayout
    var isCaps: Boolean = false
    var buttons: MutableList<Button> = mutableListOf<Button>()
    lateinit var hangulMaker: HangulMaker
    lateinit var vibrator: Vibrator
    lateinit var sharedPreferences: SharedPreferences
    var inputConnection: InputConnection? = null
        set(inputConnection) {
            field = inputConnection
        }
    var sound = 0
    var vibrate = 0
    val numpadText = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val firstLineText = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ")
    val secondLineText = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ")
    val thirdLineText = listOf("CAPS", "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ", "DEL")
    val fourthLineText = listOf("!#1", "한/영", ",", "CAM", "SPACE", ".", "ENTER")

    val firstLongClickText = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")
    val secondLongClickText = listOf("~", "+", "-", "×", "♥", ":", ";", "'", "\"")
    val thirdLongClickText = listOf("", "_", "<", ">", "/", ",", "?")

    val myKeysText = ArrayList<List<String>>()
    val myLongClickKeysText = ArrayList<List<String>>()
    val layoutLines = ArrayList<LinearLayout>()
    var downView: View? = null
    var capsView: ImageView? = null

    fun initKeyboard() {
        koreanLayout = layoutInflater.inflate(R.layout.keyboard_basic, null) as LinearLayout
        hangulMaker = HangulMaker(inputConnection!!)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)

        val height = sharedPreferences.getInt("keyboardHeight", 150)
        val config = context.getResources().configuration
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        val numpadLine = koreanLayout.findViewById<LinearLayout>(
            R.id.num_pad_line
        )
        val firstLine = koreanLayout.findViewById<LinearLayout>(
            R.id.first_line
        )
        val secondLine = koreanLayout.findViewById<LinearLayout>(
            R.id.second_line
        )
        val thirdLine = koreanLayout.findViewById<LinearLayout>(
            R.id.third_line
        )
        val fourthLine = koreanLayout.findViewById<LinearLayout>(
            R.id.fourth_line
        )

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            firstLine.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (height * 0.7).toInt()
            )
            secondLine.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (height * 0.7).toInt()
            )
            thirdLine.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (height * 0.7).toInt()
            )
        } else {
            firstLine.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
            secondLine.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
            thirdLine.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        }

        myKeysText.clear()
        myKeysText.add(numpadText)
        myKeysText.add(firstLineText)
        myKeysText.add(secondLineText)
        myKeysText.add(thirdLineText)
        myKeysText.add(fourthLineText)

        myLongClickKeysText.clear()
        myLongClickKeysText.add(firstLongClickText)
        myLongClickKeysText.add(secondLongClickText)
        myLongClickKeysText.add(thirdLongClickText)

        layoutLines.clear()
        layoutLines.add(numpadLine)
        layoutLines.add(firstLine)
        layoutLines.add(secondLine)
        layoutLines.add(thirdLine)
        layoutLines.add(fourthLine)
        setLayoutComponents()

    }

    fun getLayout(): LinearLayout {
        hangulMaker = HangulMaker(inputConnection!!)
        setLayoutComponents()
        return koreanLayout
    }


    fun modeChange() {
        if (isCaps) {
            isCaps = false
            capsView?.setImageResource(R.drawable.ic_caps_unlock)
            for (button in buttons) {
                when (button.text.toString()) {
                    "ㅃ" -> {
                        button.text = "ㅂ"
                    }
                    "ㅉ" -> {
                        button.text = "ㅈ"
                    }
                    "ㄸ" -> {
                        button.text = "ㄷ"
                    }
                    "ㄲ" -> {
                        button.text = "ㄱ"
                    }
                    "ㅆ" -> {
                        button.text = "ㅅ"
                    }
                    "ㅒ" -> {
                        button.text = "ㅐ"
                    }
                    "ㅖ" -> {
                        button.text = "ㅔ"
                    }
                }
            }
        } else {
            isCaps = true
            capsView?.setImageResource(R.drawable.ic_caps_lock)
            for (button in buttons) {
                when (button.text.toString()) {
                    "ㅂ" -> {
                        button.text = "ㅃ"
                    }
                    "ㅈ" -> {
                        button.text = "ㅉ"
                    }
                    "ㄷ" -> {
                        button.text = "ㄸ"
                    }
                    "ㄱ" -> {
                        button.text = "ㄲ"
                    }
                    "ㅅ" -> {
                        button.text = "ㅆ"
                    }
                    "ㅐ" -> {
                        button.text = "ㅒ"
                    }
                    "ㅔ" -> {
                        button.text = "ㅖ"
                    }
                }
            }
        }
    }


    private fun playClick(i: Int) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        when (i) {
            32 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            Keyboard.KEYCODE_DONE, 10 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            Keyboard.KEYCODE_DELETE -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
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

    private fun getMyClickListener(actionButton: Button): View.OnClickListener {

        val clickListener = (View.OnClickListener {
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
                hangulMaker.clear()
            }
            when (actionButton.text.toString()) {
                "한/영" -> {
                    keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.ENGLISH)
                }
                "!#1" -> {
                    keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.SYMBOLS)
                }
                "CAM" -> {
                    keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.CAMERA)
                }
                else -> {
                    playClick(actionButton.text.toString().toCharArray().get(0).toInt())
                    try {
                        val myText = Integer.parseInt(actionButton.text.toString())
                        hangulMaker.directlyCommit()
                        inputConnection?.commitText(actionButton.text.toString(), 1)
                    } catch (e: NumberFormatException) {
                        hangulMaker.commit(actionButton.text.toString().toCharArray().get(0))
                    }
                    if (isCaps) {
                        modeChange()
                    }
                }
            }
        })
        actionButton.setOnClickListener(clickListener)
        return clickListener
    }

    fun getOnTouchListener(clickListener: View.OnClickListener): View.OnTouchListener {
        val handler = Handler()
        val initailInterval = 500
        val normalInterval = 100
        val handlerRunnable = object : Runnable {
            override fun run() {
                handler.postDelayed(this, normalInterval.toLong())
                clickListener.onClick(downView)
            }
        }
        val onTouchListener = object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                when (motionEvent?.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        handler.removeCallbacks(handlerRunnable)
                        handler.postDelayed(handlerRunnable, initailInterval.toLong())
                        downView = view!!
                        clickListener.onClick(view)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacks(handlerRunnable)
                        downView = null
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        handler.removeCallbacks(handlerRunnable)
                        downView = null
                        return true
                    }
                }
                return false
            }
        }

        return onTouchListener
    }

    private fun setLayoutComponents() {
        for (line in layoutLines.indices) {
            val children = layoutLines[line].children.toList()
            val myText = myKeysText[line]
            var longClickIndex = 0
            for (item in children.indices) {
                val actionButton = children[item].findViewById<Button>(R.id.key_button)
                val specialKey = children[item].findViewById<ImageView>(R.id.special_key)
                val resourceId = when (myText[item]) {
                    "SPACE" -> R.drawable.ic_space_bar
                    "DEL" -> R.drawable.del
                    "CAPS" -> R.drawable.ic_caps_unlock
                    "ENTER" -> R.drawable.ic_enter
                    else -> 0
                }
                val myOnClickListener = when (myText[item]) {
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
                if (resourceId != 0) {
                    specialKey.setImageResource(resourceId)
                    specialKey.visibility = View.VISIBLE
                    actionButton.visibility = View.GONE
                    specialKey.setOnClickListener(myOnClickListener)
                    specialKey.setOnTouchListener(getOnTouchListener(myOnClickListener))
                    specialKey.setBackgroundResource(R.drawable.key_background)
                }
//                children[item].setOnTouchListener(getOnTouchListener(myOnClickListener))
            }
        }
    }

    fun getSpaceAction(): View.OnClickListener {
        return View.OnClickListener {
            playClick('ㅂ'.toInt())
            playVibrate()
            hangulMaker.commitSpace()
        }
    }

    fun getDeleteAction(): View.OnClickListener {
        return View.OnClickListener {
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
                hangulMaker.clear()
            } else {
                hangulMaker.delete()
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
            hangulMaker.directlyCommit()
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