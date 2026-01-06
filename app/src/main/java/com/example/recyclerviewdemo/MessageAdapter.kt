package com.example.recyclerviewdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private var messages: List<Message>,
    private val onItemClick: (Message) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        const val TYPE_UNREAD = 0
        const val TYPE_READ = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isRead) TYPE_READ else TYPE_UNREAD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == TYPE_UNREAD)
            R.layout.item_message_unread
        else
            R.layout.item_message_read

        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)

        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun updateList(newList: List<Message>) {
        val diffResult = DiffUtil.calculateDiff(
            MessageDiffCallback(messages, newList)
        )
        messages = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.title)
        private val text: TextView = itemView.findViewById(R.id.text)

        fun bind(message: Message) {
            title.text = message.title
            text.text = message.text

            itemView.setOnClickListener {
                onItemClick(message)
            }
        }
    }
}
