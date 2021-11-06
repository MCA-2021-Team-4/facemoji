package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.*
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import com.android.mca2021.keyboard.KeyboardInteractionManager
import com.android.mca2021.keyboard.*

class KeyboardSymbol constructor(
    override val context:Context,
    private val layoutInflater: LayoutInflater,
    override val keyboardInteractionListener: KeyboardInteractionManager
): FacemojiKeyboard() {
    lateinit var simbolLayout: View

    override var inputConnection:InputConnection? = null
    override val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private lateinit var sharedPreferences: SharedPreferences

    private val numpadText = listOf("1","2","3","4","5","6","7","8","9","0")
    private val firstLineText = listOf("+","×","÷","=","/","￦","<",">","♡","☆")
    private val secondLineText = listOf("!","@","#","~","%","^","&","*","(",")")
    private val thirdLineText = listOf("\uD83D\uDE00","-","'","\"",":",";",",","?","DEL")
    private val fourthLineText = listOf("가","CAM","SPACE",".","ENTER")

    override val myKeysText = listOf(
        numpadText,
        firstLineText,
        secondLineText,
        thirdLineText,
        fourthLineText
    )

    override fun changeCaps() {}

    override fun initKeyboard(){
        simbolLayout = layoutInflater.inflate(R.layout.keyboard_symbol, null) as LinearLayout

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

    override fun getLayout():View{
        return simbolLayout
    }
}