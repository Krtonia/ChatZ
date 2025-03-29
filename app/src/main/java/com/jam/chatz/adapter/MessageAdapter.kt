package com.jam.chatz.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.jam.chatz.R
import com.jam.chatz.message.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private var messages: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    // ViewHolder for sent messages
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.sent_message_text)
        val timeText: TextView = itemView.findViewById(R.id.sent_message_time)
    }

    // ViewHolder for received messages
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.received_message_text)
        val timeText: TextView = itemView.findViewById(R.id.received_message_time)
    }

    override fun getItemViewType(position: Int): Int {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sent_message, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recieved_message, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeString = formatter.format(Date(message.timestamp.seconds * 1000))

        if (holder.itemViewType == VIEW_TYPE_SENT) {
            val sentHolder = holder as SentMessageViewHolder
            sentHolder.messageText.text = message.message
            sentHolder.timeText.text = timeString
        } else {
            val receivedHolder = holder as ReceivedMessageViewHolder
            receivedHolder.messageText.text = message.message
            receivedHolder.timeText.text = timeString
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}