// AllUsers.kt
package com.jam.chatz.start.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.jam.chatz.adapter.UserAdapter
import com.jam.chatz.databinding.ActivityAllUsersBinding
import com.jam.chatz.viewmodel.UserViewModel
import androidx.activity.viewModels
import com.jam.chatz.R
import com.jam.chatz.chat.ChatActivity
import com.jam.chatz.user.User

class AllUsers : AppCompatActivity() {
    private lateinit var binding: ActivityAllUsersBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var shim: ShimmerFrameLayout
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupRecyclerView()
        setupSearchView()
        shim = binding.shimmerLayout
        shimmer(true)

        userViewModel.allUsers.observe(this) { users ->
            userAdapter.updateUsers(users)
            shimmer(false)
            checkEmptyState(users)
        }

        userViewModel.loadAllUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("USER", user)
            }
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AllUsers)
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
    }

    private fun filterUsers(query: String) {
        val currentList = userAdapter.currentList
        val filteredList = if (query.isEmpty()) {
            currentList
        } else {
            currentList.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                        user.useremail?.contains(query, ignoreCase = true) == true
            }
        }
        userAdapter.updateUsers(filteredList)
        checkEmptyState(filteredList)
    }

    private fun checkEmptyState(users: List<User>) {
        binding.noUsersFoundText.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
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
}