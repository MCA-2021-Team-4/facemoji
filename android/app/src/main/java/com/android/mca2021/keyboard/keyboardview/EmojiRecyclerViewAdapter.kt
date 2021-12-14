package com.android.mca2021.keyboard.keyboardview

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.mca2021.keyboard.R

class EmojiRecyclerViewAdapter constructor(
    private var context: Context,
    private var emojiList:ArrayList<String>,
    var inputConnection: InputConnection
    ) : RecyclerView.Adapter<EmojiRecyclerViewAdapter.Holder>(){

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val textView = itemView?.findViewById<TextView>(R.id.emoji_text)
        fun bind(emoji: String, context: Context) {
            textView?.setText(emoji)
            val onClickListener = View.OnClickListener {
                inputConnection.commitText((it as TextView).text.toString(), 1)
            }
            textView?.setOnClickListener(onClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.emoji_item, parent, false)
        return Holder(view)

    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(emojiList.get(position), context)
    }

    override fun getItemCount(): Int {
        return emojiList.size
    }
}