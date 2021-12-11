package com.android.mca2021.keyboard.keyboardview


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.android.mca2021.keyboard.FacemojiService
import com.android.mca2021.keyboard.R

class EmojiWeightPopup: AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.weight_option)
        sharedPreferences = this.baseContext.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()

        seekBarConfig()

        btnConfirm = findViewById(R.id.confirm_button)

        val intent = Intent(this.baseContext, FacemojiService::class.java)
        btnConfirm.setOnClickListener {
            sharedPreferencesEditor.putInt("anger", weightArray[0])
            sharedPreferencesEditor.putInt("disgust", weightArray[1])
            sharedPreferencesEditor.putInt("fear", weightArray[2])
            sharedPreferencesEditor.putInt("happiness", weightArray[3])
            sharedPreferencesEditor.putInt("neutral", weightArray[4])
            sharedPreferencesEditor.putInt("sadness", weightArray[5])
            sharedPreferencesEditor.putInt("surprise", weightArray[6])
            sharedPreferencesEditor.apply()
            finish()

        }

    }
    fun seekBarConfig() {
        seekBarAnger = findViewById(R.id.seekBar_anger)
        seekBarDisgust = findViewById(R.id.seekBar_disgust)
        seekBarFear = findViewById(R.id.seekBar_fear)
        seekBarHappiness = findViewById(R.id.seekBar_happiness)
        seekBarNeutral = findViewById(R.id.seekBar_neutral)
        seekBarSadness = findViewById(R.id.seekBar_sadness)
        seekBarSurprise = findViewById(R.id.seekBar_surprise)

        weightArray[0] = sharedPreferences.getInt("anger", 0)
        weightArray[1] = sharedPreferences.getInt("disgust", 0)
        weightArray[2] = sharedPreferences.getInt("fear", 0)
        weightArray[3] = sharedPreferences.getInt("happiness", 0)
        weightArray[4] = sharedPreferences.getInt("neutral", 0)
        weightArray[5] = sharedPreferences.getInt("sadness", 0)
        weightArray[6] = sharedPreferences.getInt("surprise", 0)

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
                if (p0 == seekBarAnger) {
                    weightArray[0] = p0.progress
                }
                else if (p0 == seekBarDisgust) {
                    weightArray[1] = p0.progress
                }
                else if (p0 == seekBarFear) {
                    weightArray[2] = p0.progress
                }
                else if (p0 == seekBarHappiness) {
                    weightArray[3] = p0.progress
                }
                else if (p0 == seekBarNeutral) {
                    weightArray[4] = p0.progress
                }
                else if (p0 == seekBarSadness) {
                    weightArray[5] = p0.progress
                }
                else if (p0 == seekBarSurprise) {
                    weightArray[6] = p0.progress
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

    override fun onBackPressed() {

    }



}