package com.jam.chatz.start.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.jam.chatz.R
import com.jam.chatz.adapter.UserAdapter
import com.jam.chatz.databinding.ActivityHomeBinding
import com.jam.chatz.user.User
import com.jam.chatz.viewmodel.UserViewModel

@SuppressLint("SetTextI18n")
class Home : AppCompatActivity() {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var shim: ShimmerFrameLayout
    private val userViewModel: UserViewModel by viewModels()
    private var allUsers: List<User> = emptyList()
    private lateinit var auth: FirebaseAuth
    private val binding: ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    //onCreate function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(binding.root)
        setupRecyclerView()
        setupSearchView()
        shim = binding.shimmerLayout
        shimmer(true)
        userViewModel.users.observe(this, Observer { users ->
            userAdapter.updateUsers(users)
            shimmer(false)
        })
        userViewModel.loadUsers()
        binding.swipeRefreshLayout.setOnRefreshListener {
            userViewModel.loadUsers()
            shimmer(true)
        }
        userViewModel.users.observe(
            this, Observer { users -> binding.swipeRefreshLayout.isRefreshing = false })
        binding.fab.setOnClickListener {

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
        userRecyclerView = findViewById(R.id.usersRecyclerView)
        userAdapter = UserAdapter(emptyList())
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter
        observeUsers()
    }

    private fun setupSearchView() {
        val searchCard: MaterialCardView = findViewById(R.id.searchCard)
        val searchView: SearchView = findViewById(R.id.searchView)
        searchCard.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterUsers(query: String) {
        var nouserfoundtext: TextView = findViewById(R.id.noUsersFoundText)
        val filteredList = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.username?.contains(
                    query, ignoreCase = true
                ) == true || user.useremail?.contains(query, ignoreCase = true) == true
            }
        }
        userAdapter.updateUsers(filteredList)
        if (query.isNotEmpty() && filteredList.isEmpty()) {
            nouserfoundtext.text = "No users found matching '$query'"
            nouserfoundtext.visibility = View.VISIBLE
        } else if (filteredList.isEmpty()) {
            nouserfoundtext.text = "No users found"
            nouserfoundtext.visibility = View.VISIBLE
        } else {
            nouserfoundtext.visibility = View.GONE
        }
    }

    private fun observeUsers() {
        userViewModel.users.observe(this) { users ->
            allUsers = users
            if (users.isNotEmpty()) {
                userAdapter.updateUsers(users)
                findViewById<TextView>(R.id.noUsersFoundText).visibility = View.GONE
            } else {
                findViewById<TextView>(R.id.noUsersFoundText).apply {
                    text = "No users found"
                    visibility = View.VISIBLE
                }
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}