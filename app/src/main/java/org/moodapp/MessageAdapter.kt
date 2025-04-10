package org.moodapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.moodapp.databinding.ItemMessageBinding

class MessageAdapter(private var messageList: MutableList<MessageEntity>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)
    companion object {
        class MessageDiffCallback(private val oldList: List<MessageEntity>, private val newList: List<MessageEntity>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.binding.messageTextView.text = message.message
    }

    override fun getItemCount(): Int = messageList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition].id == newList[newItemPosition].id
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
     fun updateData(newMessages: List<MessageEntity>) {
            val diffResult = DiffUtil.calculateDiff(MessageDiffCallback(messageList, newMessages))
            messageList.clear()
            messageList.addAll(newMessages)
            diffResult.dispatchUpdatesTo(this)
        }

}