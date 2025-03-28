package com.jam.chatz.start.Home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jam.chatz.R
import com.jam.chatz.adapter.UserAdapter
import com.jam.chatz.viewmodel.UserViewModel

class Home : AppCompatActivity() {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Setup RecyclerView
        userRecyclerView = findViewById(R.id.usersRecyclerView)
        userAdapter = UserAdapter(emptyList())
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        // Observe users from ViewModel
        observeUsers()
    }

    private fun observeUsers() {
        userViewModel.users.observe(this) { users ->
            if (users.isNotEmpty()) {
                userAdapter.updateUsers(users)
                Log.d("Home", "Users loaded: ${users.size}")
            } else {
                Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
                Log.d("Home", "No users available")
            }
        }
    }
}