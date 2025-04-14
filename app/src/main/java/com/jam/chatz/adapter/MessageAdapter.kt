package com.jam.chatz.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.jam.chatz.R
import com.jam.chatz.message.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private var messages: List<Message>,private val onImageClickListener: OnImageClickListener? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //Testing Image opening functionality
    interface OnImageClickListener {
        fun onImageClick(imageUrl: String)
    }

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val items = mutableListOf<Any>()

    companion object {
        private const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_SENT_TEXT = 1
        private const val VIEW_TYPE_SENT_IMAGE = 2
        private const val VIEW_TYPE_RECEIVED_TEXT = 3
        private const val VIEW_TYPE_RECEIVED_IMAGE = 4
    }

    init {
        processMessages()
    }

    private fun processMessages() {
        items.clear()
        var lastDate: String? = null
        messages.sortedBy { it.timestamp.seconds }.forEach { message ->
            if (message.isImage && message.imageUrl.isNullOrEmpty()) {
                return@forEach
            }
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

    class SentTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.sent_message_text)
        val timeText: TextView = itemView.findViewById(R.id.sent_message_time)
    }

    class SentImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageImage: ImageView = itemView.findViewById(R.id.message_image)
        val timeText: TextView = itemView.findViewById(R.id.time_text)
        val progressBar: ProgressBar = itemView.findViewById(R.id.image_progress)
    }

    class ReceivedTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.received_message_text)
        val timeText: TextView = itemView.findViewById(R.id.received_message_time)
    }

    class ReceivedImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageImage: ImageView = itemView.findViewById(R.id.message_image)
        val timeText: TextView = itemView.findViewById(R.id.time_text)
        val progressBar: ProgressBar = itemView.findViewById(R.id.image_progress)
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is DateHeader -> VIEW_TYPE_DATE_HEADER
            is Message -> {
                when {
                    item.senderId == currentUserId && item.type == "image" && !item.imageUrl.isNullOrEmpty() -> VIEW_TYPE_SENT_IMAGE
                    item.senderId == currentUserId -> VIEW_TYPE_SENT_TEXT
                    item.senderId != currentUserId && item.type == "image" && !item.imageUrl.isNullOrEmpty() -> VIEW_TYPE_RECEIVED_IMAGE
                    else -> VIEW_TYPE_RECEIVED_TEXT
                }
            }

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        VIEW_TYPE_DATE_HEADER -> DateHeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
        )

        VIEW_TYPE_SENT_TEXT -> SentTextViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sent_message, parent, false)
        )

        VIEW_TYPE_SENT_IMAGE -> SentImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent_image, parent, false)
        )

        VIEW_TYPE_RECEIVED_TEXT -> ReceivedTextViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recieved_message, parent, false)
        )

        VIEW_TYPE_RECEIVED_IMAGE -> ReceivedImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_message_recieved_image, parent, false)
        )

        else -> throw IllegalArgumentException("Invalid view type")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DateHeader -> (holder as DateHeaderViewHolder).dateText.text = item.date
            is Message -> {
                val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date(item.timestamp.seconds * 1000))

                when (holder) {
                    is SentTextViewHolder -> {
                        holder.messageText.text = item.text
                        holder.timeText.text = timeString
                    }

                    is SentImageViewHolder -> {
                        bindImageMessage(holder, item, timeString)
                    }

                    is ReceivedTextViewHolder -> {
                        holder.messageText.text = item.text
                        holder.timeText.text = timeString
                    }

                    is ReceivedImageViewHolder -> {
                        bindImageMessage(holder, item, timeString)
                    }
                }
            }
        }
    }

    private fun bindImageMessage(
        holder: Any,
        message: Message,
        timeString: String
    ) {
        when (holder) {
            is SentImageViewHolder -> {
                holder.progressBar.visibility = View.VISIBLE
                holder.timeText.text = timeString
                Log.d("MessageAdapter", "Loading sent image from URL: ${message.imageUrl}")
                message.imageUrl?.let { url ->
                    loadImageWithGlide(url, holder.messageImage, holder.progressBar)
                    holder.messageImage.setOnClickListener {
                        onImageClickListener?.onImageClick(url)
                    }
                }
            }

            is ReceivedImageViewHolder -> {
                holder.progressBar.visibility = View.VISIBLE
                holder.timeText.text = timeString
                Log.d("MessageAdapter", "Loading received image from URL: ${message.imageUrl}")
                message.imageUrl?.let { url ->
                    loadImageWithGlide(url, holder.messageImage, holder.progressBar)
                    holder.messageImage.setOnClickListener {
                        onImageClickListener?.onImageClick(url)
                    }
                }
            }
        }
    }

    private fun loadImageWithGlide(
        imageUrl: String,
        imageView: ImageView,
        progressBar: ProgressBar
    ) {
        Log.d("MessageAdapter", "Loading image from: $imageUrl")
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            Glide.with(imageView.context)
                .load(imageUrl)
                .override(600, 600)
                .centerCrop()
                .addListener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        Log.e("Glide", "Failed to load image: $imageUrl", e)
                        imageView.setImageResource(R.drawable.img_1)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(imageView)
        } else {
            progressBar.visibility = View.GONE
            Log.e("MessageAdapter", "Invalid image URL: $imageUrl")
            imageView.setImageResource(R.drawable.img_1)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateMessages(newMessages: List<Message>) {
        val validMessages = newMessages.filter { message ->
            if (message.isImage && message.imageUrl.isNullOrEmpty()) {
                false
            } else {
                true
            }
        }

        messages = validMessages
        processMessages()
        notifyDataSetChanged()
    }

    private fun getFormattedDate(timestamp: Long): String {
        return SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(timestamp))
    }

    private data class DateHeader(val date: String)
}