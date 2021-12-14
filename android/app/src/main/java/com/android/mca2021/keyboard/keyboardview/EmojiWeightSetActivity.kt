package com.android.mca2021.keyboard.keyboardview


import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.mca2021.keyboard.R

class EmojiWeightSetActivity: AppCompatActivity() {
    private lateinit var btnConfirm: Button
    private var weightArray: IntArray = IntArray(7) {0}
    private lateinit var seekBarAnger: SeekBar
    private lateinit var seekBarDisgust: SeekBar
    private lateinit var seekBarFear: SeekBar
    private lateinit var seekBarHappiness: SeekBar
    private lateinit var seekBarNeutral: SeekBar
    private lateinit var seekBarSadness: SeekBar
    private lateinit var seekBarSurprise: SeekBar
    private lateinit var sharedPreferences: SharedPreferences

    private val emojisUnicode = listOf(
        "\uD83D\uDE21",
        "\uD83D\uDE23",
        "\uD83D\uDE28",
        "\uD83D\uDE42",
        "\uD83D\uDE10",
        "\uD83D\uDE1E",
        "\uD83D\uDE2E",
    )

    private val emojiItemIds = listOf(
        R.id.emoji_anger,
        R.id.emoji_disgust,
        R.id.emoji_fear,
        R.id.emoji_happiness,
        R.id.emoji_neutral,
        R.id.emoji_sadness,
        R.id.emoji_surprise
    )

    private val emojiNames = listOf(
        "anger",
        "disgust",
        "fear",
        "happiness",
        "neutral",
        "sadness",
        "surprise"
    )

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.weight_option)
        sharedPreferences = this.baseContext.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()

        seekBarConfig()

        Handler(Looper.getMainLooper()).post {
            setEmojiLayout()
        }

        btnConfirm = findViewById(R.id.confirm_button)
        btnConfirm.setOnClickListener {
            emojiNames.forEachIndexed { idx, emoji ->
                sharedPreferencesEditor.putInt(emoji, weightArray[idx])
            }
            sharedPreferencesEditor.apply()
            finish()
        }

    }

    private fun setEmojiLayout() {
            emojiItemIds.forEachIndexed { index, id ->
                val textView = findViewById<View>(id).findViewById<TextView>(R.id.emoji_text)
                textView.text = emojisUnicode[index]
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun seekBarConfig() {
        seekBarAnger = findViewById(R.id.seekBar_anger)
        seekBarDisgust = findViewById(R.id.seekBar_disgust)
        seekBarFear = findViewById(R.id.seekBar_fear)
        seekBarHappiness = findViewById(R.id.seekBar_happiness)
        seekBarNeutral = findViewById(R.id.seekBar_neutral)
        seekBarSadness = findViewById(R.id.seekBar_sadness)
        seekBarSurprise = findViewById(R.id.seekBar_surprise)

        emojiNames.forEachIndexed { idx, emoji ->
            weightArray[idx] = sharedPreferences.getInt(emoji, 0)
        }

        seekBarAnger.setProgress(weightArray[0], true)
        seekBarDisgust.setProgress(weightArray[1], true)
        seekBarFear.setProgress(weightArray[2], true)
        seekBarHappiness.setProgress(weightArray[3], true)
        seekBarNeutral.setProgress(weightArray[4], true)
        seekBarSadness.setProgress(weightArray[5], true)
        seekBarSurprise.setProgress(weightArray[6], true)

        val listener = object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                when (p0) {
                    seekBarAnger -> weightArray[0] = p0.progress
                    seekBarDisgust -> weightArray[1] = p0.progress
                    seekBarFear -> weightArray[2] = p0.progress
                    seekBarHappiness -> weightArray[3] = p0.progress
                    seekBarNeutral -> weightArray[4] = p0.progress
                    seekBarSadness -> weightArray[5] = p0.progress
                    seekBarSurprise -> weightArray[6] = p0.progress
                }
            }
        }
        seekBarAnger.setOnSeekBarChangeListener(listener)
        seekBarDisgust.setOnSeekBarChangeListener(listener)
        seekBarFear.setOnSeekBarChangeListener(listener)
        seekBarHappiness.setOnSeekBarChangeListener(listener)
        seekBarNeutral.setOnSeekBarChangeListener(listener)
        seekBarSadness.setOnSeekBarChangeListener(listener)
        seekBarSurprise.setOnSeekBarChangeListener(listener)
    }

}