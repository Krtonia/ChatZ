package com.jam.chatz.start.Home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jam.chatz.R
import com.jam.chatz.adapter.UserAdapter
import com.jam.chatz.user.User
import com.jam.chatz.viewmodel.UserViewModel

class Home : AppCompatActivity() {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userViewModel: UserViewModel by viewModels()
    private var allUsers: List<User> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        setupSearchView()

        // Setup RecyclerView

        userRecyclerView = findViewById(R.id.usersRecyclerView)
        userAdapter = UserAdapter(emptyList())
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        // Observe users from ViewModel
        observeUsers()

        //Test
        val searchCard: MaterialCardView = findViewById(R.id.searchCard)
        val searchView: SearchView = findViewById(R.id.searchView)


        searchCard.setOnClickListener {
            searchView.isIconified = false // Expand SearchView when clicked
        }
    }

    private fun observeUsers() {

        userViewModel.users.observe(this) { users ->
            allUsers = users
            if (users.isNotEmpty()) {
                userAdapter.updateUsers(users)
                Log.d("Home", "Users loaded: ${users.size}")
            } else {
                Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
                Log.d("Home", "No users available")
            }
        }
    }

    private fun setupSearchView() {
        val searchCard: MaterialCardView = findViewById(R.id.searchCard)
        val searchView: SearchView = findViewById(R.id.searchView)

        searchCard.setOnClickListener {
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText.orEmpty())
                return true
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun filterUsers(query: String) {
        var nouserfoundtext: TextView = findViewById(R.id.noUsersFoundText)
        val filteredList = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                        user.useremail?.contains(query, ignoreCase = true) == true
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
}

