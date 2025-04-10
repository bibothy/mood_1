package org.moodapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.moodapp.MessageEntity
import org.moodapp.R

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        private const val TAG = "MessageAdapter"
    }

    private var messages: List<MessageEntity> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder called for position: $position")
        val message = messages[position]
        holder.messageText.text = "${message.sender}: ${message.text}"
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount called, returning: ${messages.size}")
        return messages.size
    }

    fun updateMessages(newMessages: List<MessageEntity>) {
        Log.d(TAG, "updateMessages called, new message count: ${newMessages.size}")
        messages = newMessages
        notifyDataSetChanged()
        Log.d(TAG, "updateMessages finished")

    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }
}