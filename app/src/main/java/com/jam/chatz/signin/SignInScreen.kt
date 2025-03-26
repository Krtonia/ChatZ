package com.jam.chatz.signin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jam.chatz.databinding.ActivitySignInScreenBinding

class SignInScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySignInScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the binding
        binding = ActivitySignInScreenBinding.inflate(layoutInflater)

        // Set the content view to the root of the binding
        setContentView(binding.root)

        // Set up login button click listener
//        binding.loginButton.setOnClickListener {
//            val email = binding.emailId.toString()
//            val password = binding.password.text.toString()

//            // Perform login validation
//            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        // Implement your login logic here
//        when {
//            email.isEmpty() -> binding.emailId.error = "Email cannot be empty"
//            password.isEmpty() -> binding.password.error = "Password cannot be empty"
//            else -> {
//                // Clear previous errors
//                binding.emailId.error = null
//                binding.password.error = null

                // Perform actual login
                // For example: viewModel.login(email, password)
            }
//        }
//    }
//}