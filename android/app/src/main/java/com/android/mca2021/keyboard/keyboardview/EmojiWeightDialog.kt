package com.android.mca2021.keyboard.keyboardview

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import com.android.mca2021.keyboard.R

class EmojiWeightDialog(context: Context) {
    private val dlg = Dialog(context)
    private lateinit var btnConfirm: Button
    private var weightArray: FloatArray = FloatArray(7) {1f}
    private lateinit var seekBarAnger: SeekBar
    private lateinit var confirmListener: EmojiWeightDialogListner

    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.weight_option)
        dlg.setCancelable(false)

        seekBarAnger = dlg.findViewById(R.id.seekBar_anger)

        val seekBarListener = object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    if (seekBar == seekBarAnger) {
                        weightArray[0] = 1f + 100 * (seekBar?.progress?.div(7f) ?: 0f)
                    }
                }
            }
        }

        seekBarAnger.setOnSeekBarChangeListener(seekBarListener)

        btnConfirm = dlg.findViewById(R.id.confirm_button)
        btnConfirm.setOnClickListener {
            confirmListener.onConfirmClicked(weightArray)
            dlg.dismiss()
        }
        dlg.show()
    }

    fun setOnConfirmClickedListener(listener: (FloatArray) -> Unit) {
        this.confirmListener = object: EmojiWeightDialogListner {
            override fun onConfirmClicked(content: FloatArray) {
                listener(content)
            }
        }
    }


    interface EmojiWeightDialogListner {
        fun onConfirmClicked(content: FloatArray)
    }
}