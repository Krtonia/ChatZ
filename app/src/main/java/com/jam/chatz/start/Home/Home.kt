package com.jam.chatz.start.Home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.jam.chatz.R
import com.jam.chatz.adapter.UserAdapter
import com.jam.chatz.databinding.ActivityHomeBinding
import com.jam.chatz.start.signin.SignInScreen
import com.jam.chatz.user.User
import com.jam.chatz.viewmodel.UserViewModel

class Home : AppCompatActivity() {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userViewModel: UserViewModel by viewModels()
    private var allUsers: List<User> = emptyList()
    private lateinit var auth: FirebaseAuth
    private val binding : ActivityHomeBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(binding.root)
        setupRecyclerView()
        setupSearchView()
        clicklistener()
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

    private fun clicklistener(){
        binding.moreButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, SignInScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun observeUsers() {
        userViewModel.users.observe(this) { users ->
            allUsers = users
            if (users.isNotEmpty()) {
                userAdapter.updateUsers(users)
            } else {
                Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                        user.useremail?.contains(query, ignoreCase = true) == true
            }
        }
        userAdapter.updateUsers(filteredList)
    }
}