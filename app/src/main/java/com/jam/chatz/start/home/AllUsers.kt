@file:Suppress("DEPRECATION")

package com.jam.chatz.start.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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
    private var originalUsers: List<User> = emptyList()
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
            originalUsers = users
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
                user.username?.contains(
                    query, ignoreCase = true
                ) == true || user.useremail?.contains(query, ignoreCase = true) == true
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