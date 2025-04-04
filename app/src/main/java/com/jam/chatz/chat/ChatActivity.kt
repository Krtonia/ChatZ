package com.jam.chatz.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.jam.chatz.R
import com.jam.chatz.user.User
import com.jam.chatz.databinding.ActivityChatBinding
import com.jam.chatz.message.Message

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val chatViewModel: ChatViewModel by viewModels()
    private var otherUser: User? = null
    private val allMessages = mutableListOf<Message>()
    private var realTimeListenerActive = false

    private var isLoading = false
    private var isLastPage = false
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val pageSize = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        otherUser = intent.getParcelableExtra("USER")
        if (otherUser == null) {
            Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupToolbar()
        setupRecyclerView()
        loadInitialMessages()
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                otherUser?.userid?.let { userId ->
                    binding.messageInput.isEnabled = false
                    chatViewModel.sendMessage(userId, messageText) { success ->
                        binding.messageInput.isEnabled = true
                        if (success) {
                            binding.messageInput.text?.clear()
                            scrollToBottom()
                        } else {
                            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.chatUsername.text = otherUser?.username
        binding.userkastatus.text = "Hey There I'm using Chatz!"
        binding.chatToolbar.elevation = 20f
        Glide.with(this).load(otherUser?.imageurl).placeholder(R.drawable.img).circleCrop()
            .into(binding.chatUserImage)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(allMessages) // Use the combined list
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        binding.messagesRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = messageAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    if (!isLoading && !isLastPage && firstVisibleItemPosition <= 5 && totalItemCount >= pageSize) {
                        loadMoreMessages()
                    }
                }
            })
        }
    }

    private fun loadInitialMessages() {
        isLoading = true
        otherUser?.userid?.let { userId ->
            setupRealTimeListener(userId)
            realTimeListenerActive = true
            chatViewModel.getInitialMessages(userId, pageSize).observe(this) { messages ->
                allMessages.clear()
                allMessages.addAll(messages)
                messageAdapter.notifyDataSetChanged()
                isLoading = false

                if (messages.isNotEmpty()) {
                    lastVisibleDocument = chatViewModel.getLastVisibleDocument()
                    if (messages.size < pageSize) {
                        isLastPage = true
                    }
                }
            }
        }
    }

    private fun setupRealTimeListener(userId: String) {
        chatViewModel.getMessages(userId).observe(this) { newMessages ->
            val recentNewMessages = newMessages.filter { newMsg ->
                !allMessages.any { existingMsg -> existingMsg.messageId == newMsg.messageId }
            }
            if (recentNewMessages.isNotEmpty()) {
                allMessages.addAll(recentNewMessages)
                allMessages.sortBy { it.timestamp.seconds }
                messageAdapter.notifyDataSetChanged()
                messageAdapter.updateMessages(allMessages)
            }
        }
    }

    private fun loadMoreMessages() {
        if (isLoading || isLastPage || lastVisibleDocument == null) return
        isLoading = true
        otherUser?.userid?.let { userId ->
            chatViewModel.loadMoreMessages(userId, lastVisibleDocument!!, pageSize)
                .observe(this) { messages ->
                    if (messages.isNotEmpty()) {
                        allMessages.addAll(0, messages)
                        allMessages.sortBy { it.timestamp.seconds }
                        messageAdapter.notifyDataSetChanged()
                        lastVisibleDocument = chatViewModel.getLastVisibleDocument()
                        if (messages.size < pageSize) {
                            isLastPage = true
                        }
                    } else {
                        isLastPage = true
                    }
                    isLoading = false
                }
        }
    }

    private fun scrollToBottom() {
        if (allMessages.isEmpty()) return
        binding.messagesRecyclerView.post {
            val layoutManager = binding.messagesRecyclerView.layoutManager as LinearLayoutManager
            layoutManager.scrollToPositionWithOffset(allMessages.size - 1, 0)
        }
    }
}
