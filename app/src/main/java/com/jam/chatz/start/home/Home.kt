package com.jam.chatz.start.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.jam.chatz.R
import com.jam.chatz.adapter.UserAdapter
import com.jam.chatz.chat.ChatActivity
import com.jam.chatz.databinding.ActivityHomeBinding
import com.jam.chatz.user.User
import com.jam.chatz.viewmodel.UserViewModel

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var shim: ShimmerFrameLayout
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var originalUsers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setupRecyclerView()
        setupBottomNavigation()
        setupSearchView()
        shim = binding.shimmerLayout
        loadConversationUsers()
        binding.swipeRefreshLayout.setOnRefreshListener { loadConversationUsers() }
        binding.fab.setOnClickListener {
            val intent = Intent(this, AllUsers::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                else -> false
            }
        }

        // Set the selected item to highlight the correct tab
        binding.bottomNavigation.selectedItemId = R.id.nav_home
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
            binding.noUsersFoundText.text = "No conversations yet\nStart a new chat by tapping the button"
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
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home)
            adapter = userAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText.orEmpty())
                return true
            }
        })
        binding.searchView.setOnCloseListener {
            resetSearch()
            false
        }
    }

    private fun resetSearch() {
        binding.searchView.setQuery("", false)
        userViewModel.loadAllUsers()
        userAdapter.updateUsers(originalUsers)
        checkEmptyState(originalUsers)
        binding.searchView.isIconified = true
    }

    private fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalUsers
        } else {
            originalUsers.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                        user.useremail?.contains(query, ignoreCase = true) == true
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