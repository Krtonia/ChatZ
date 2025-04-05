package com.jam.chatz.adapter

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

    var currentList = messages.toMutableList()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_DATE_HEADER = 0
    }

    private val items = mutableListOf<Any>()

    init {
        processMessages()
    }

    private fun processMessages() {
        items.clear()
        var lastDate: String? = null
        messages.sortedBy { it.timestamp.seconds }.forEach { message ->
            val currentDate = getFormattedDate(message.timestamp.seconds * 1000)
            if (lastDate == null || currentDate != lastDate) {
                items.add(DateHeader(currentDate))
                lastDate = currentDate
            }
            items.add(message)
        }
    }

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.dateHeader)
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.sent_message_text)
        val timeText: TextView = itemView.findViewById(R.id.sent_message_time)
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.received_message_text)
        val timeText: TextView = itemView.findViewById(R.id.received_message_time)
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is DateHeader -> VIEW_TYPE_DATE_HEADER
            is Message -> {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (item.senderId == currentUserId) {
                    VIEW_TYPE_SENT
                } else {
                    VIEW_TYPE_RECEIVED
                }
            }

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }

            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_sent_message, parent, false)
                SentMessageViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recieved_message, parent, false)
                ReceivedMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DateHeader -> {
                (holder as DateHeaderViewHolder).dateText.text = item.date
            }

            is Message -> {
                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeString = formatter.format(Date(item.timestamp.seconds * 1000))

                if (holder.itemViewType == VIEW_TYPE_SENT) {
                    val sentHolder = holder as SentMessageViewHolder
                    sentHolder.messageText.text = item.message
                    sentHolder.timeText.text = timeString
                } else {
                    val receivedHolder = holder as ReceivedMessageViewHolder
                    receivedHolder.messageText.text = item.message
                    receivedHolder.timeText.text = timeString
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateMessages(newMessages: List<Message>) {
        currentList.clear()
        currentList.addAll(newMessages)
        messages = newMessages
        processMessages()
        notifyDataSetChanged()
    }

    private fun getFormattedDate(timestamp: Long): String {
        return SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(timestamp))
    }

    private data class DateHeader(val date: String)
}