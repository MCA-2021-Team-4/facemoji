package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import android.media.AudioManager
import android.os.*
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import com.android.mca2021.keyboard.KeyboardInteractionListener
import com.android.mca2021.keyboard.*

class KeyboardSymbol constructor(
    var context:Context,
    var layoutInflater: LayoutInflater,
    var keyboardInterationListener: KeyboardInteractionListener
) {
    lateinit var simbolLayout: View
    var inputConnection:InputConnection? = null
    var buttons:MutableList<Button> = mutableListOf<Button>()
    lateinit var vibrator: Vibrator
    private lateinit var sharedPreferences: SharedPreferences



    private val numpadText = listOf("1","2","3","4","5","6","7","8","9","0")
    private val firstLineText = listOf("+","×","÷","=","/","￦","<",">","♡","☆")
    private val secondLineText = listOf("!","@","#","~","%","^","&","*","(",")")
    private val thirdLineText = listOf("\uD83D\uDE00","-","'","\"",":",";",",","?","DEL")
    private val fourthLineText = listOf("가","한/영",",","SPACE",".","ENTER")

    private val myKeysText = listOf(
        numpadText,
        firstLineText,
        secondLineText,
        thirdLineText,
        fourthLineText
    )
    private lateinit var layoutLines: List<LinearLayout>

    var downView:View? = null
    var vibrate = 0
    var sound = 0

    fun initKeyboard(){
        simbolLayout = layoutInflater.inflate(R.layout.keyboard_symbol, null) as LinearLayout
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val numPadLine: LinearLayout = simbolLayout.findViewById(R.id.num_pad_line)
        val firstLine: LinearLayout = simbolLayout.findViewById(R.id.first_line)
        val secondLine: LinearLayout = simbolLayout.findViewById(R.id.second_line)
        val thirdLine: LinearLayout = simbolLayout.findViewById(R.id.third_line)
        val fourthLine: LinearLayout = simbolLayout.findViewById(R.id.fourth_line)

        val config = context.resources.configuration
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        val preferredHeight = sharedPreferences.getFloat("keyboardHeight", 250f) / 5.0f
        val heightInDp =
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) preferredHeight * 0.7f
            else preferredHeight
        val height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightInDp,
            context.resources.displayMetrics
        ).toInt()

        numPadLine.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        firstLine.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        secondLine.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        thirdLine.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        fourthLine.layoutParams =
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

    fun getLayout():View{
        return simbolLayout
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


    private fun playVibrate(){
        if(vibrate > 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(70, vibrate))
            }
            else{
                vibrator.vibrate(70)
            }
        }
    }

    private fun getMyClickListener(actionButton:Button):View.OnClickListener{

        val clickListener = (View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                inputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
            }
            playVibrate()
            val cursorcs:CharSequence? =  inputConnection?.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
            if(cursorcs != null && cursorcs.length >= 2){

                val eventTime = SystemClock.uptimeMillis()
                inputConnection?.finishComposingText()
                inputConnection?.sendKeyEvent(KeyEvent(eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))
                inputConnection?.sendKeyEvent(KeyEvent(SystemClock.uptimeMillis(), eventTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))
            }

            when (actionButton.text.toString()) {
                "\uD83D\uDE00" -> {
                    keyboardInterationListener.changeMode(KeyboardInteractionListener.KeyboardType.EMOJI)
                }
                "한/영" -> {
                    keyboardInterationListener.changeMode(KeyboardInteractionListener.KeyboardType.ENGLISH)
                }
                "가" -> {
                    keyboardInterationListener.changeMode(KeyboardInteractionListener.KeyboardType.KOREAN)
                }
                else -> {
                    playClick(
                        actionButton.text.toString().toCharArray().get(
                            0
                        ).toInt()
                    )
                    inputConnection?.commitText(actionButton.text.toString(), 1)
                }
            }

        })
        actionButton.setOnClickListener(clickListener)
        return clickListener
    }

    fun getOnTouchListener(clickListener: View.OnClickListener):View.OnTouchListener{
        val handler = Handler()
        val initailInterval = 500
        val normalInterval = 100
        val handlerRunnable = object: Runnable{
            override fun run() {
                handler.postDelayed(this, normalInterval.toLong())
                clickListener.onClick(downView)
            }
        }
        val onTouchListener = object:View.OnTouchListener {
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

    private fun setLayoutComponents(){
        layoutLines.forEachIndexed{ line, layout ->
            val children = layout.children.toList()
            val myText = myKeysText[line]
            children.forEachIndexed { item, child ->
                val actionButton = child.findViewById<Button>(R.id.key_button)
                val specialKey = child.findViewById<ImageView>(R.id.special_key)
                var myOnClickListener:View.OnClickListener? = null
                val resourceId = when (myText[item]) {
                    "SPACE" -> R.drawable.ic_space_bar
                    "DEL" -> R.drawable.del
                    "ENTER" -> R.drawable.ic_enter
                    else -> 0
                }
                val onClickListener = when (myText[item]) {
                    "SPACE" -> getSpaceAction()
                    "DEL" -> getDeleteAction()
                    "ENTER" -> getEnterAction()
                    else -> {
                        actionButton.text = myText[item]
                        buttons.add(actionButton)
                        getMyClickListener(actionButton)
                    }
                }
                if (resourceId == 0) {
                    actionButton.setOnTouchListener(getOnTouchListener(onClickListener))
                } else {
                    specialKey.setImageResource(resourceId)
                    specialKey.visibility = View.VISIBLE
                    actionButton.visibility = View.GONE
                    specialKey.setOnClickListener(onClickListener)
                    specialKey.setOnTouchListener(getOnTouchListener(onClickListener))
                    specialKey.setBackgroundResource(R.drawable.key_background)
                }
                children[item].setOnClickListener(myOnClickListener)
            }
        }
    }
    fun getSpaceAction():View.OnClickListener{
        return View.OnClickListener{
            playClick('ㅂ'.toInt())
            playVibrate()
            inputConnection?.commitText(" ",1)
        }
    }

    fun getDeleteAction():View.OnClickListener{
        return View.OnClickListener{
            playVibrate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                inputConnection?.deleteSurroundingTextInCodePoints(1, 0)
            }else{
                inputConnection?.deleteSurroundingText(1,0)
            }
        }
    }


    fun getEnterAction():View.OnClickListener{
        return View.OnClickListener{
            playVibrate()
            val eventTime = SystemClock.uptimeMillis()
            inputConnection?.sendKeyEvent(KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD))
            inputConnection?.sendKeyEvent(KeyEvent(SystemClock.uptimeMillis(), eventTime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD))
        }
    }


}