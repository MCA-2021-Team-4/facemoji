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
    override val context:Context,
    private val layoutInflater: LayoutInflater,
    override val keyboardInteractionListener: KeyboardInteractionManager,
): FacemojiKeyboard() {
    private val emotionUnicode: List<Int> = listOf(0x1F600, 0x1F910, 0x1F920, 0x1F928, 0x1F973)
    private val animalUnicode: List<Int> = listOf(0x1F408, 0x1F409, 0x1F40C, 0x1F40F, 0x1F411, 0x1F413, 0x1F414, 0x1F415, 0x1F416, 0x1F417, 0x1F42B)
    private val foodUnicode: List<Int> = listOf(0x1F347)
    private val placeUnicode: List<Int> = listOf(0x1F30D)
    private val skyUnicode: List<Int> = listOf(0x1F311)

    private val emotionSize: List<Int> = listOf(64, 9, 8, 7, 4)
    private val animalSize: List<Int> = listOf(1, 3, 3, 2, 2, 1 ,1, 1, 1, 19, 20)
    private val foodSize: List<Int> = listOf(100)
    private val placeSize: List<Int> = listOf(100)
    private val skySize: List<Int> = listOf(30)

    lateinit var emojiLayout: View
    override var inputConnection: InputConnection? = null
    override val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    lateinit var emojiRecyclerViewAdapter: EmojiRecyclerViewAdapter
    private val fourthLineText = listOf(
        "한/영",
        getEmojiByUnicode(emotionUnicode[0]),
        getEmojiByUnicode(animalUnicode[0]),
        getEmojiByUnicode(foodUnicode[0]),
        getEmojiByUnicode(placeUnicode[0]),
        getEmojiByUnicode(skyUnicode[0]),
        "DEL"
    )

    override fun initKeyboard() {
        emojiLayout = layoutInflater.inflate(R.layout.keyboard_emoji, null) as LinearLayout

        val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        vibrate = sharedPreferences.getInt("vibrate", -1)
        sound = sharedPreferences.getInt("sound", -1)

        val fourthLine: LinearLayout = emojiLayout.findViewById(R.id.fourth_line)
        fourthLine.forEachIndexed { item, child ->
            val actionButton = child.findViewById<Button>(R.id.key_button)
            val specialKey = child.findViewById<ImageView>(R.id.special_key)
            if (fourthLineText[item].equals("DEL")) {
                val myOnClickListener = getDeleteAction()
                val myOnTouchListener = getOnTouchListener(myOnClickListener)
                specialKey.setImageResource(R.drawable.del)
                specialKey.visibility = View.VISIBLE
                actionButton.visibility = View.GONE
                specialKey.setOnClickListener(myOnClickListener)
                specialKey.setOnTouchListener(myOnTouchListener)
                specialKey.setBackgroundResource(R.drawable.key_background)
            } else {
                actionButton.text = fourthLineText[item]
                actionButton.setOnClickListener(View.OnClickListener {
                    when ((it as Button).text) {
                        "한/영" -> {
                            keyboardInteractionListener.changeMode(KeyboardInteractionManager.KeyboardType.ENGLISH)
                        }
                        getEmojiByUnicode(emotionUnicode[0]) -> {
                            setLayoutComponents(emotionUnicode, emotionSize)
                        }
                        getEmojiByUnicode(animalUnicode[0]) -> {
                            setLayoutComponents(animalUnicode, animalSize)
                        }
                        getEmojiByUnicode(foodUnicode[0]) -> {
                            setLayoutComponents(foodUnicode, foodSize)
                        }
                        getEmojiByUnicode(placeUnicode[0]) -> {
                            setLayoutComponents(placeUnicode, placeSize)
                        }
                        getEmojiByUnicode(skyUnicode[0]) -> {
                            setLayoutComponents(skyUnicode, skySize)
                        }
                    }
                })
            }
        }

        setLayoutComponents(emotionUnicode, emotionSize)
    }

    override fun getLayout():View{
        return emojiLayout
    }

    private fun setLayoutComponents(unicodes: List<Int>, counts: List<Int>) {
        val recyclerView = emojiLayout.findViewById<RecyclerView>(R.id.emoji_recyclerview)
        val emojiList = ArrayList<String>()
        val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val height = sharedPreferences.getInt("keyboardHeight", 150)

        for (i in unicodes.indices){
            var unicode = unicodes[i]
            var count = counts[i]
            for (j in 0..count){
                emojiList.add(getEmojiByUnicode(unicode + j))
            }
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

    override fun changeCaps() {}
}