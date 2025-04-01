package com.jam.chatz.start.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.databinding.ActivitySignupScreenBinding
import com.jam.chatz.start.signin.SignInScreen

class SignUpScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignupScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button - go to SignIn screen
        binding.lgbtn.setOnClickListener {
            navigateToSignIn()
        }

        // Register button
        binding.regbtn.setOnClickListener {
            val name = binding.signupname.text?.trim().toString()
            val email = binding.signupemail.text?.trim().toString()
            val pass = binding.signuppassword.text?.trim().toString()
            val cnfpass = binding.signupconfirmpassword.text?.trim().toString()

            when {
                name.isEmpty() -> showError("Please enter your name")
                email.isEmpty() -> showError("Please enter your email")
                pass.isEmpty() -> showError("Please enter a password")
                cnfpass.isEmpty() -> showError("Please confirm your password")
                pass != cnfpass -> showError("Passwords don't match")
                pass.length < 6 -> showError("Password must be at least 6 characters")
                else -> registerUser(name, email, pass)
            }
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToFirestore(name, email)
                } else {
                    showError("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserToFirestore(name: String, email: String) {
        auth.currentUser?.let { user ->
            val userData = hashMapOf(
                "userid" to user.uid,
                "username" to name,
                "useremail" to email,
                "status" to "Hey there! I'm using ChatZ",
                "imageurl" to "https://www.pngarts.com/files/5/User-Avatar-PNG-Image.png"
            )

            firestore.collection("Users").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    showSuccess("Registration successful!")
                    // Sign out the automatically logged-in user
                    auth.signOut()
                    navigateToSignIn()
                }
                .addOnFailureListener { e ->
                    showError("Failed to save user data: ${e.message}")
                }
        } ?: showError("User creation failed")
    }

    private fun navigateToSignIn() {
        startActivity(Intent(this, SignInScreen::class.java)).apply {
            var flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            finish()
        }

    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}