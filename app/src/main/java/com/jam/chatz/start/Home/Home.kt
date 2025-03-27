package com.jam.chatz.start.Home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.R
import com.jam.chatz.User

class Home : AppCompatActivity() {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Setup RecyclerView
        userRecyclerView = findViewById(R.id.usersRecyclerView)
        userAdapter = UserAdapter(users)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        // Fetch users from Firestore
        fetchUsers()
    }

    private fun fetchUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                users.clear()
                for (document in querySnapshot.documents) {
                    val user = User(
                        id = document.id,
                        username = document.getString("username") ?: "",
                        email = document.getString("email") ?: "",
                        profilePictureUrl = document.getString("profilePictureUrl")
                    )
                    users.add(user)
                }
                userAdapter.updateUsers(users)
            }
            .addOnFailureListener { exception ->
            }
    }
}