package com.example.moodup.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moodup.databinding.RecyclerViewReceiverBinding
import com.example.moodup.databinding.RecyclerViewSenderBinding
import com.example.moodup.ui.chat.business.Chat


class ChatAdapter(
    private val onClickCallback: (message: String, view: View) -> Unit
) :
    ListAdapter<Chat, RecyclerView.ViewHolder>(DiffCallback()) {


    class SenderViewHolder(private val recyclerViewSenderBinding: RecyclerViewSenderBinding) :
        RecyclerView.ViewHolder(recyclerViewSenderBinding.root) {
        fun bind(chat: Chat) {
            recyclerViewSenderBinding.senderMessage.text = chat.message
        }
    }

    class ReceiverViewHolder(private val recyclerViewReceiverBinding: RecyclerViewReceiverBinding) :
        RecyclerView.ViewHolder(recyclerViewReceiverBinding.root) {
        fun bind(chat: Chat) {
            recyclerViewReceiverBinding.receiverMessage.text = chat.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) { //Receiver
            ReceiverViewHolder(
                RecyclerViewReceiverBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else { //Sender
            SenderViewHolder(
                RecyclerViewSenderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = getItem(position)
        if (chat.messageType.equals("sender", true)) {
            (holder as SenderViewHolder).bind(chat)
        } else {
            (holder as ReceiverViewHolder).bind(chat)
        }
        holder.itemView.setOnLongClickListener {
            if (holder.adapterPosition != -1) {
                onClickCallback(chat.message, holder.itemView)
            }
            true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).messageType.equals("sender", true)) {
            0 // Sender
        } else {
            1 // Receiver
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }

    }


}