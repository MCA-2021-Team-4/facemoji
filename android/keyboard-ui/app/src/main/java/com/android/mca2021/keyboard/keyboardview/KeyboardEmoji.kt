package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.mca2021.keyboard.*


class KeyboardEmoji constructor(
    var context:Context,
    var layoutInflater: LayoutInflater,
    private var keyboardInteractionListener: KeyboardInteractionListener
){
    val emotionUnicode = 0x1F600
    val animalUnicode = 0x1F435
    val foodUnicode = 0x1F347
    val placeUnicode = 0x1F30D
    val skyUnicode = 0x1F311

    val emotionSize = 146
    val animalSize = 100
    val foodSize = 100
    val placeSize = 100
    val skySize = 30

    lateinit var emojiLayout: View
    var inputConnection: InputConnection? = null
    lateinit var vibrator: Vibrator

    lateinit var emojiRecyclerViewAdapter: EmojiRecyclerViewAdapter
    private val fourthLineText = listOf(
        "한/영",
        getEmojiByUnicode(emotionUnicode),
        getEmojiByUnicode(animalUnicode),
        getEmojiByUnicode(foodUnicode),
        getEmojiByUnicode(placeUnicode),
        getEmojiByUnicode(skyUnicode),
        "DEL"
    )
    var vibrate = 0
    var sound = 0

    fun initKeyboard() {
        emojiLayout = layoutInflater.inflate(R.layout.keyboard_emoji, null) as LinearLayout
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        vibrate = sharedPreferences.getInt("vibrate", -1)
        sound = sharedPreferences.getInt("sound", -1)

        val fourthLine: LinearLayout = emojiLayout.findViewById(R.id.fourth_line)
        fourthLine.forEachIndexed { item, child ->
            val actionButton = child.findViewById<Button>(R.id.key_button)
            val spacialKey = child.findViewById<ImageView>(R.id.special_key)
            if (fourthLineText[item].equals("DEL")) {
                actionButton.setBackgroundResource(R.drawable.del)
                val myOnClickListener = getDeleteAction()
                actionButton.setOnClickListener(myOnClickListener)
            } else {
                actionButton.text = fourthLineText[item]
                actionButton.setOnClickListener(View.OnClickListener {
                    when ((it as Button).text) {
                        "한/영" -> {
                            keyboardInteractionListener.changeMode(KeyboardInteractionListener.KeyboardType.ENGLISH)
                        }
                        getEmojiByUnicode(emotionUnicode) -> {
                            setLayoutComponents(emotionUnicode, emotionSize)
                        }
                        getEmojiByUnicode(animalUnicode) -> {
                            setLayoutComponents(animalUnicode, animalSize)
                        }
                        getEmojiByUnicode(foodUnicode) -> {
                            setLayoutComponents(foodUnicode, foodSize)
                        }
                        getEmojiByUnicode(placeUnicode) -> {
                            setLayoutComponents(placeUnicode, placeSize)
                        }
                        getEmojiByUnicode(skyUnicode) -> {
                            setLayoutComponents(skyUnicode, skySize)
                        }
                    }
                })
            }
        }

        setLayoutComponents(0x1F600, 79)
    }

    fun getLayout():View{
        return emojiLayout
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

    private fun setLayoutComponents(unicode: Int, count: Int) {
        var recyclerView = emojiLayout.findViewById<RecyclerView>(R.id.emoji_recyclerview)
        val emojiList = ArrayList<String>()
        val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val height = sharedPreferences.getInt("keyboardHeight", 150)


        for (i in 0..count) {
            emojiList.add(getEmojiByUnicode(unicode + i))
        }

        emojiRecyclerViewAdapter = EmojiRecyclerViewAdapter(context, emojiList, inputConnection!!)
        recyclerView.adapter = emojiRecyclerViewAdapter
        val gm = GridLayoutManager(context, 8)
        gm.isItemPrefetchEnabled = true
        recyclerView.layoutManager = gm
        recyclerView.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height * 5)
    }

    fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    fun getDeleteAction(): View.OnClickListener {
        return View.OnClickListener {
            playVibrate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                inputConnection!!.deleteSurroundingTextInCodePoints(1, 0)
            } else {
                inputConnection!!.deleteSurroundingText(1, 0)
            }
        }
    }

}