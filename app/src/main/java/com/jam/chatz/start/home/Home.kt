package com.jam.chatz.start.home

//noinspection SuspiciousImport
import android.R
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Log.e
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.adapter.UserAdapter
import com.jam.chatz.chat.ChatActivity
import com.jam.chatz.databinding.ActivityHomeBinding
import com.jam.chatz.user.User
import com.jam.chatz.viewmodel.UserViewModel

@Suppress("DEPRECATION")
class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var shim: ShimmerFrameLayout
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var originalUsers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        enableEdgeToEdge()
        setupRecyclerView()
        setupSearchView()
        shim = binding.shimmerLayout
        loadConversationUsers()
        loadCurrentUserProfile()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadConversationUsers()
            loadCurrentUserProfile()
        }

        binding.profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, AllUsers::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        }
    }

    override fun onResume() {
        super.onResume()

        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        val currentUser = auth.currentUser ?: return

        db.collection("Users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("imageurl")) {
                    val imageUrl = document.getString("imageurl")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(com.jam.chatz.R.drawable.img)
                            .circleCrop()
                            .into(binding.profile)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("Home","Failed to load profile picture")
            }
    }

    private fun loadConversationUsers() {
        shimmer(true)
        userViewModel.loadUsersWithConversations()
        userViewModel.conversationUsers.observe(this) { users ->
            originalUsers = users
            userAdapter.updateUsers(users)
            shimmer(false)
            checkEmptyState(users)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun checkEmptyState(users: List<User>) {
        if (users.isEmpty()) {
            binding.noUsersFoundText.text = buildString {
                append("No conversations yet\nStart a new chat by tapping the button below")
            }
            binding.noUsersFoundText.visibility = View.VISIBLE
        } else {
            binding.noUsersFoundText.visibility = View.GONE
        }
    }

    private fun shimmer(show: Boolean) {
        if (show) {
            shim.visibility = View.VISIBLE
            shim.startShimmer()
            binding.usersRecyclerView.visibility = View.GONE
        } else {
            shim.visibility = View.GONE
            shim.stopShimmer()
            binding.usersRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("USER", user)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home)
            adapter = userAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) >= 1) {
                    filterUsers(s.toString())
                } else {
                    userViewModel.loadAllUsers()
                    userAdapter.updateUsers(originalUsers)
                    checkEmptyState(originalUsers)
                }
            }
        })

        binding.searchTextView.setEndIconOnClickListener {
            resetSearch()
        }
    }

    private fun resetSearch() {
        binding.searchText.setText("")
        userViewModel.loadAllUsers()
        userAdapter.updateUsers(originalUsers)
        checkEmptyState(originalUsers)
    }

    private fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalUsers
        } else {
            originalUsers.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true || user.useremail?.contains(query, ignoreCase = true) == true
            }
        }
        userAdapter.updateUsers(filteredList)
        checkEmptyState(filteredList)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}