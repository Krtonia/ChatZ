package com.jam.chatz.start.signin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.jam.chatz.databinding.ActivitySignInScreenBinding
import com.jam.chatz.databinding.ActivitySignupScreenBinding
import com.jam.chatz.start.Home.Home
import com.jam.chatz.start.signup.SignUpScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignInScreen : AppCompatActivity() {

    private val binding: ActivitySignInScreenBinding by lazy {
        ActivitySignInScreenBinding.inflate(layoutInflater)
    }

    override fun onStart() {
        super.onStart()

        var auth: FirebaseAuth
        auth = FirebaseAuth.getInstance()

        //Check if User already logged in
        val user: FirebaseUser? = auth.currentUser

        if (user != null)
            startActivity(Intent(this, Home::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var auth: FirebaseAuth
        auth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        binding.regbtn.setOnClickListener { startActivity(Intent(this, SignUpScreen::class.java)) }
        binding.lgbtn.setOnClickListener {
            var email = binding.email.text.toString()
            var pass = binding.pass.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            }
            else {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successfull", Toast.LENGTH_LONG)
                                .show()
                            startActivity(Intent(this, Home::class.java))
                        } else {
                            Toast.makeText(this, "Login failed ${task.exception.toString()}", Toast.LENGTH_LONG).show()
                        }
                    }

            }
        }
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var auth: FirebaseAuth
        auth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        binding.regbtn.setOnClickListener { startActivity(Intent(this, SignUpScreen::class.java)) }
        binding.lgbtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val pass = binding.pass.text.toString().trim()

            fun handleLoginError(exception: Exception?) {
                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                    is FirebaseAuthInvalidUserException -> "No account found with this email"
                    else -> "Login failed: ${exception?.localizedMessage}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }

            fun performFirebaseLogin(email: String, password: String) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        binding.lgbtn.isEnabled = true
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Home::class.java))
                            finish()
                        } else {
                            handleLoginError(task.exception)
                        }
                    }
            }

            when {
                email.isEmpty() -> binding.email.error = "Email cannot be empty"
                pass.isEmpty() -> binding.pass.error = "Password cannot be empty"
                pass.length < 6 -> binding.pass.error = "Password too short"
                else -> {
                    binding.lgbtn.isEnabled = false
                    performFirebaseLogin(email, pass)
                }
            }
        }
    }*/
}


