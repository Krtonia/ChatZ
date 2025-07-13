package com.jam.chatz.start.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.R
import com.jam.chatz.databinding.ActivityProfileBinding
import com.jam.chatz.start.signin.SignInScreen

@SuppressLint("SetTextI18n")
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logoutBtn.setOnClickListener { showLogoutConfirmation() }
        loadUserProfile()
        binding.changeUname.setOnClickListener { openDialog() }
        binding.changeStatus.setOnClickListener { statusDialog() }
        binding.imgedit.setOnClickListener { Toast.makeText(this,"Edit button is being clicked", Toast.LENGTH_SHORT).show() }
    }

    private fun openDialog() {
        val dialogView = layoutInflater.inflate(R.layout.item_username, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.editText)
        TextInputEditText.AUTOFILL_HINT_NAME
        val dialog = MaterialAlertDialogBuilder(this).setView(dialogView).setPositiveButton("Confirm") { _, _ ->
            val inputText = editText.text.toString().trim()
            if (!TextUtils.isEmpty(inputText)) {
                updateUsername(inputText)
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }.setNegativeButton("Cancel", null).create()

        dialog.show()
    }

    private fun statusDialog() {
        val dialogView = layoutInflater.inflate(R.layout.item_status, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.editText)
        TextInputEditText.AUTOFILL_TYPE_TEXT
        val dialog = MaterialAlertDialogBuilder(this).setView(dialogView).setPositiveButton("Confirm") { _, _ ->
            val inputText = editText.text.toString().trim()
            if (!TextUtils.isEmpty(inputText)) {
                updateStatus(inputText)
            } else {
                Toast.makeText(this, "Status cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }.setNegativeButton("Cancel", null).create()

        dialog.show()
    }

    private fun updateUsername(newUsername: String) {
        val currentUser = auth.currentUser ?: run {
            startActivity(Intent(this, SignInScreen::class.java))
            finish()
            return
        }

        val userData = hashMapOf(
            "username" to newUsername
        )

        db.collection("Users").document(currentUser.uid).update(userData as Map<String, Any>).addOnSuccessListener {
            binding.usernameText.text = "Username: $newUsername"
            Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to update Username: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStatus(newStatus: String) {
        val currentUser = auth.currentUser ?: run {
            startActivity(Intent(this, SignInScreen::class.java))
            finish()
            return
        }

        val userData = hashMapOf(
            "status" to newStatus
        )

        db.collection("Users").document(currentUser.uid).update(userData as Map<String, Any>).addOnSuccessListener {
            binding.userstatus.text = "Status: $newStatus"
            Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to update Status: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser ?: run {
            startActivity(Intent(this, SignInScreen::class.java))
            finish()
            return
        }

        //Loading User-profile image
        db.collection("Users").document(currentUser.uid).get().addOnSuccessListener { document ->
            if (document != null && document.contains("imageurl")) {
                val imageUrl = document.getString("imageurl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.img).into(binding.profileimg)
                }
            }
        }

        //Email
        binding.emailText.text = "Email: ${currentUser.email ?: "Not available"}"
        db.collection("Users").document(currentUser.uid).get().addOnSuccessListener { document ->
            //Status
            binding.userstatus.text = "Status: ${document.getString("status") ?: "Unknown"}"
            //Username
            binding.usernameText.text = "Username: ${document.getString("username") ?: "Unknown"}"
        }.addOnFailureListener {
            binding.usernameText.text = "Username: Error loading"
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this).setTitle("Logout").setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, SignInScreen::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.show()
    }
}