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
import com.android.mca2021.keyboard.*
import java.util.*


class KeyboardEnglish constructor(
    override val context: Context,
    private val layoutInflater: LayoutInflater,
    override val keyboardInteractionListener: KeyboardInteractionManager
): FacemojiKeyboard() {
    private lateinit var englishLayout: View

    override var inputConnection: InputConnection? = null
    override val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private lateinit var sharedPreferences: SharedPreferences

    val setting: String = "setting"
    val vibrateSetting: String = "keyboardVibrate"
    val soundSetting: String = "keyboardSound"

    private val numPadText = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val firstLineText = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    private val secondLineText = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    private val thirdLineText = listOf("CAPS", "z", "x", "c", "v", "b", "n", "m", "DEL")
    private val fourthLineText = listOf("!#1", "한/영", "CAM", "SPACE", ".", "ENTER")

    private val firstLongClickText = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")")
    private val secondLongClickText = listOf("~", "+", "-", "×", "♥", ":", ";", "'", "\"")
    private val thirdLongClickText = listOf("∞", "_", "<", ">", "/", ",", "?")

    override val myKeysText = listOf(
        numPadText,
        firstLineText,
        secondLineText,
        thirdLineText,
        fourthLineText,
    )

    override val myLongClickKeysText = listOf(
        firstLongClickText,
        secondLongClickText,
        thirdLongClickText,
    )

    override lateinit var layoutLines: List<LinearLayout>

    override fun initKeyboard() {
        englishLayout = layoutInflater.inflate(R.layout.keyboard_basic, null)

        val numPadLine: LinearLayout = englishLayout.findViewById(R.id.num_pad_line)
        val firstLine: LinearLayout = englishLayout.findViewById(R.id.first_line)
        val secondLine: LinearLayout = englishLayout.findViewById(R.id.second_line)
        val thirdLine: LinearLayout = englishLayout.findViewById(R.id.third_line)
        val fourthLine: LinearLayout = englishLayout.findViewById(R.id.fourth_line)

        val config = context.resources.configuration
        sharedPreferences = context.getSharedPreferences(setting, Context.MODE_PRIVATE)
        sound = sharedPreferences.getInt(soundSetting, -1)
        vibrate = sharedPreferences.getInt(vibrateSetting, -1)

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

    override fun getLayout(): View {
        return englishLayout
    }

    override fun changeCaps() {
        isCaps = !isCaps
        buttons.filter{ it.text != "CAM" }.forEach {
            it.text =
                if (isCaps) it.text.toString().lowercase(Locale.US)
                else it.text.toString().uppercase(Locale.US)
        }
    }
}