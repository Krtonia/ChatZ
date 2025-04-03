package com.jam.chatz.start.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.databinding.ActivitySignInScreenBinding
import com.jam.chatz.start.Home.Home
import com.jam.chatz.start.signup.SignUpScreen
import com.jam.chatz.user.User

class SignInScreen : AppCompatActivity() {
    private val binding: ActivitySignInScreenBinding by lazy {
        ActivitySignInScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        alreadyLogin()

        var auth: FirebaseAuth
        auth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        binding.regbtn.setOnClickListener { startActivity(Intent(this, SignUpScreen::class.java)) }
        binding.lgbtn.setOnClickListener {
            var email = binding.email.text.toString()
            var pass = binding.pass.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            } else {binding.pgbar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successfull", Toast.LENGTH_LONG)
                                .show()
                            binding.pgbar.visibility = View.GONE
                            startActivity(Intent(this, Home::class.java))
                        } else {
                            Toast.makeText(
                                this,
                                "Login failed ${task.exception.toString()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

            }
        }
    }

    private fun alreadyLogin() {
        var auth: FirebaseAuth
        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
    }
}


