package com.jam.chatz.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.jam.chatz.R
import com.jam.chatz.adapter.MessageAdapter
import com.jam.chatz.databinding.ActivityChatBinding
import com.jam.chatz.imgur.Uploader
import com.jam.chatz.message.Message
import com.jam.chatz.start.home.Home
import com.jam.chatz.user.User
import com.jam.chatz.viewmodel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity(), MessageAdapter.OnImageClickListener {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val chatViewModel: ChatViewModel by viewModels()
    private var otherUser: User? = null
    private val allMessages = mutableListOf<Message>()
    private var realTimeListenerActive = false
    private var isLoading: Boolean = false
    private var isLastPage = false
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val pageSize = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        binding.back.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        otherUser = intent.getParcelableExtra("USER")
        if (otherUser == null) {
            Toast.makeText(this, "User data not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.godbtn.setOnClickListener { scrollToBottom() }
        binding.godbtn.visibility = View.GONE
        binding.imgup.setOnClickListener { showImageChooser() }

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
                        }
                    }
                }
            } else if (messageText.isBlank() || messageText.isEmpty()) {
                Toast.makeText(this, "Can't send an empty message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onImageClick(imageUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(imageUrl.toUri(), "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app available to view images ${e}", Toast.LENGTH_SHORT).show()
        }
    }

    private val imageChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            uri?.let { imageUri ->
                uploadAndSendImage(imageUri)
            }
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        val chooser = Intent.createChooser(fileIntent, "Select Image")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(galleryIntent))
        imageChooserLauncher.launch(chooser)
    }


    private fun uploadAndSendImage(imageUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.messageInput.isEnabled = false
        binding.sendButton.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imageUrl = Uploader.uploadImage(this@ChatActivity, imageUri)

                withContext(Dispatchers.Main) {
                    if (imageUrl != null) {
                        otherUser?.userid?.let { userId ->
                            chatViewModel.sendImageMessage(userId, imageUrl) { success ->
                                binding.progressBar.visibility = View.GONE
                                binding.messageInput.isEnabled = true
                                binding.sendButton.isEnabled = true

                                if (!success) {
                                    Toast.makeText(
                                        this@ChatActivity, "Failed to send image", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.messageInput.isEnabled = true
                        binding.sendButton.isEnabled = true
                        Toast.makeText(
                            this@ChatActivity, "Failed to upload image", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.messageInput.isEnabled = true
                    binding.sendButton.isEnabled = true
                    Toast.makeText(
                        this@ChatActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupToolbar() {
        binding.chatUsername.text = otherUser?.username
        binding.userkastatus.text = "Hey There I am using Chatz!"
        binding.chatToolbar.elevation = 20f
        Glide.with(this).load(otherUser?.imageurl).placeholder(R.drawable.img).circleCrop().into(binding.chatUserImage)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(allMessages, this)
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
                    val pos = layoutManager.findLastVisibleItemPosition()
                    if (pos < totalItemCount - 15) {
                        binding.godbtn.visibility = View.VISIBLE
                    } else {
                        binding.godbtn.visibility = View.GONE
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

    // Update the real-time listener setup
    private fun setupRealTimeListener(userId: String) {
        chatViewModel.getMessages(userId).observe(this) { newMessages ->
            val recentNewMessages = newMessages.filter { newMsg ->
                !allMessages.any { existingMsg -> existingMsg.messageId == newMsg.messageId }
            }

            if (recentNewMessages.isNotEmpty()) {
                allMessages.addAll(recentNewMessages)
                allMessages.sortBy { it.timestamp.seconds }
                messageAdapter.updateMessages(allMessages)

                val lastMessage = allMessages.lastOrNull()
                if (lastMessage != null && lastMessage.senderId == chatViewModel.getCurrentUserId()) {
                    scrollToBottom()
                }
            }
        }
    }

    private fun loadMoreMessages() {
        if (isLoading || isLastPage || lastVisibleDocument == null) return
        isLoading = true
        otherUser?.userid?.let { userId ->
            chatViewModel.loadMoreMessages(userId, lastVisibleDocument!!, pageSize).observe(this) { messages ->
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
        binding.messagesRecyclerView.post {
            val itemCount = messageAdapter.itemCount
            if (itemCount > 0) {
                binding.messagesRecyclerView.smoothScrollToPosition(itemCount - 1)
            }
        }
    }
}