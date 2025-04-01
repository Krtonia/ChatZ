package com.jam.chatz.start.signup

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.databinding.ActivitySignupScreenBinding
import com.jam.chatz.start.signin.SignInScreen

class SignUpScreen : AppCompatActivity() {

    var isLoading = false

    private val binding: ActivitySignupScreenBinding by lazy {
        ActivitySignupScreenBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        binding.lgbtn.setOnClickListener {
            startActivity(Intent(this, SignInScreen::class.java))
        }

        binding.regbtn.setOnClickListener {

            val name = binding.signupname.text?.trim().toString()
            val email = binding.signupemail.text?.trim().toString()
            val pass = binding.signuppassword.text?.trim().toString()
            val cnfpass = binding.signupconfirmpassword.text?.trim().toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || cnfpass.isEmpty()) {
                Toast.makeText(this, "Please fill all required details", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (pass != cnfpass) {
                Toast.makeText(
                    this, "Password and confirm password do not match", Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            isLoading = true
            binding
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User created successfully, now save to Firestore
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        val userMap = hashMapOf(
                            "userid" to firebaseUser.uid,
                            "username" to name,
                            "useremail" to email,
                            "status" to "Hey there! I'm using ChatZ",
                            "imageurl" to "https://www.pngarts.com/files/5/User-Avatar-PNG-Image.png"
                        )

                        firestore.collection("Users").document(firebaseUser.uid).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this, "Registration successful!", Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(this, SignInScreen::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                                finish()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    this,
                                    "Failed to save user data: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
