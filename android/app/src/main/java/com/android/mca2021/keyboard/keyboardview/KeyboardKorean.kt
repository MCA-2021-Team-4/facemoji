package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.*
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.mca2021.keyboard.*
import java.lang.NumberFormatException

class KeyboardKorean constructor(
    override val context: Context,
    private val layoutInflater: LayoutInflater,
    override val keyboardInteractionListener: KeyboardInteractionManager
): FacemojiKeyboard() {
    private lateinit var koreanLayout: LinearLayout

    override var inputConnection: InputConnection? = null
    override val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private lateinit var hangulMaker: HangulMaker
    private lateinit var sharedPreferences: SharedPreferences

    private val numPadText = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val firstLineText = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ")
    private val secondLineText = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ")
    private val thirdLineText = listOf("CAPS", "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ", "DEL")
    private val fourthLineText = listOf("!#1", "한/영", "CAM", "SPACE", ".", "ENTER")

    var capsView: ImageView? = null

    override val myKeysText = listOf(
        numPadText,
        firstLineText,
        secondLineText,
        thirdLineText,
        fourthLineText,
    )

    override fun initKeyboard() {
        koreanLayout = layoutInflater.inflate(R.layout.keyboard_basic, null) as LinearLayout
        hangulMaker = HangulMaker(inputConnection!!)

        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)

        val config = context.getResources().configuration

        val preferredHeight = sharedPreferences.getFloat("keyboardHeight", 250f) / 5.0f
        val heightInDp =
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) preferredHeight * 0.7f
            else preferredHeight
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightInDp,
            context.resources.displayMetrics
        ).toInt()
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        val numPadLine = koreanLayout.findViewById<LinearLayout>(
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

        layoutLines = listOf(
            numPadLine,
            firstLine,
            secondLine,
            thirdLine,
            fourthLine,
        )

        setLayoutComponents()
    }

    override fun getLayout(): LinearLayout {
        hangulMaker = HangulMaker(inputConnection!!)
        setLayoutComponents()
        return koreanLayout
    }

    override fun changeLanguage() {
        keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.ENGLISH)
    }

    override fun changeCaps() {
        if (isCaps) {
            isCaps = false
            capsView?.setImageResource(R.drawable.ic_caps_unlock)
            for (button in buttons) {
                val textView = button.findViewById<TextView>(R.id.key_text)
                when (textView.text.toString()) {
                    "ㅃ" -> {
                        textView.text = "ㅂ"
                    }
                    "ㅉ" -> {
                        textView.text = "ㅈ"
                    }
                    "ㄸ" -> {
                        textView.text = "ㄷ"
                    }
                    "ㄲ" -> {
                        textView.text = "ㄱ"
                    }
                    "ㅆ" -> {
                        textView.text = "ㅅ"
                    }
                    "ㅒ" -> {
                        textView.text = "ㅐ"
                    }
                    "ㅖ" -> {
                        textView.text = "ㅔ"
                    }
                }
            }
        } else {
            isCaps = true
            capsView?.setImageResource(R.drawable.ic_caps_lock)
            for (button in buttons) {
                val textView = button.findViewById<TextView>(R.id.key_text)
                when (textView.text.toString()) {
                    "ㅂ" -> {
                        textView.text = "ㅃ"
                    }
                    "ㅈ" -> {
                        textView.text = "ㅉ"
                    }
                    "ㄷ" -> {
                        textView.text = "ㄸ"
                    }
                    "ㄱ" -> {
                        textView.text = "ㄲ"
                    }
                    "ㅅ" -> {
                        textView.text = "ㅆ"
                    }
                    "ㅐ" -> {
                        textView.text = "ㅒ"
                    }
                    "ㅔ" -> {
                        textView.text = "ㅖ"
                    }
                }
            }
        }
    }

    override fun handleTextButton(actionButton: View) {
        val textView = actionButton.findViewById<TextView>(R.id.key_text)
        playClick(textView.text.toString().toCharArray().get(0).toInt())
        try {
            Integer.parseInt(textView.text.toString())
            hangulMaker.directlyCommit()
            inputConnection?.commitText(textView.text.toString(), 1)
        } catch (e: NumberFormatException) {
            hangulMaker.commit(textView.text.toString().toCharArray().get(0))
        }
        if (isCaps) {
            changeCaps()
        }
    }



    override fun getSpaceAction(): View.OnClickListener {
        return View.OnClickListener {
            playClick('ㅂ'.toInt())
            playVibrate()
            hangulMaker.commitSpace()
        }
    }

    override fun getDeleteAction(): View.OnClickListener {
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
}