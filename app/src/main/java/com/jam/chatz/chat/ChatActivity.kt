package com.jam.chatz.chat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.R
import com.jam.chatz.user.User
import com.jam.chatz.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val chatViewModel: ChatViewModel by viewModels()
    private var otherUser: User? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun setUserOnlineStatus(online: Boolean) {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("Users")
                .document(userId)
                .update(mapOf(
                    "status" to if (online) "Online" else "Offline",
                    "lastSeen" to System.currentTimeMillis()
                ))
                .addOnFailureListener { e ->
                    Log.e("Presence", "Failed to update status", e)
                    // Add retry logic if needed
                }
        }
    }

    override fun onStart() {
        super.onStart()
        setUserOnlineStatus(online = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        setUserOnlineStatus(online = false)
    }

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
        observeMessages()
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                otherUser?.userid?.let { userId ->
                    chatViewModel.sendMessage(userId, messageText) { success ->
                        if (success) {
                            binding.messageInput.text?.clear()
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
        binding.userkastatus.text = otherUser?.status?: "Offline"

        // Add real-time status listener
        otherUser?.userid?.let { userId ->
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("STATUS_ERROR", "Error listening to status", error)
                        return@addSnapshotListener
                    }

                    val status = snapshot?.getString("status") ?: "Offline"
                    binding.userkastatus.text = status
                    Log.d("STATUS_UPDATE", "New status: $status") // Debug log
                }
        }

        Glide.with(this)
            .load(otherUser?.imageurl)
            .placeholder(R.drawable.img)
            .circleCrop()
            .into(binding.chatUserImage)
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList())
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun observeMessages() {
        otherUser?.userid?.let { userId ->
            chatViewModel.getMessages(userId).observe(this) { messages ->
                messageAdapter.updateMessages(messages)
                if (messages.isNotEmpty()) {
                    binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }
    }
}