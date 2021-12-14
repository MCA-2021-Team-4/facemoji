package com.android.mca2021.keyboard.keyboardview


import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.mca2021.keyboard.EmojiPlatform
import com.android.mca2021.keyboard.R

class EmojiWeightSetActivity: AppCompatActivity() {
    private lateinit var cameraFrame: FrameLayout
    private var weightArray: IntArray = IntArray(7) {0}
    private lateinit var seekBarAnger: SeekBar
    private lateinit var seekBarDisgust: SeekBar
    private lateinit var seekBarFear: SeekBar
    private lateinit var seekBarHappiness: SeekBar
    private lateinit var seekBarNeutral: SeekBar
    private lateinit var seekBarSadness: SeekBar
    private lateinit var seekBarSurprise: SeekBar
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var weightSetCamera: WeightSetCamera
    private var mEmojiPlatform : EmojiPlatform = EmojiPlatform.GOOGLE

    private val emojiItemtoEmojiId = mapOf(
        R.id.emoji_anger to 0,
        R.id.emoji_disgust to 6,
        R.id.emoji_fear to 37,
        R.id.emoji_happiness to 45,
        R.id.emoji_neutral to 58,
        R.id.emoji_sadness to 9,
        R.id.emoji_surprise to 3
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
        sharedPreferences = baseContext.getSharedPreferences("setting", Context.MODE_PRIVATE)

        setEmojiLayout()
        cameraFrame = findViewById(R.id.camera_frame)
        weightSetCamera = WeightSetCamera(this, applicationContext, assets, layoutInflater)

        seekBarConfig()
    }

    public override fun onStart() {
        super.onStart()
        weightSetCamera.initCamera()
        cameraFrame.addView(weightSetCamera.getLayout())
    }

    public override fun onStop() {
        super.onStop()
        weightSetCamera.finishCamera()

    }

    private fun setEmojiLayout() {
        mEmojiPlatform = EmojiPlatform.from(sharedPreferences.getString("emojiPlatform", "google")!!)
        for(itemId in emojiItemtoEmojiId.keys){
            val imageView = findViewById<ImageView>(itemId)
            val id: Int = applicationContext.resources.getIdentifier("zzz_${mEmojiPlatform.name.lowercase()}_${emojiItemtoEmojiId.get(itemId)}", "drawable", applicationContext.packageName)
            val bmp = BitmapFactory.decodeResource(applicationContext.resources, id)
            imageView.setImageBitmap(bmp)
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
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
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
                val sharedPreferencesEditor = sharedPreferences.edit()
                emojiNames.forEachIndexed { idx, emoji ->
                    sharedPreferencesEditor.putInt(emoji, weightArray[idx])
                }
                sharedPreferencesEditor.apply()
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