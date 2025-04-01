package org.moodapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.moodapp.MessageEntity
import org.moodapp.R

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var messages: List<MessageEntity> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = "${message.sender}: ${message.text}"
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<MessageEntity>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }
}