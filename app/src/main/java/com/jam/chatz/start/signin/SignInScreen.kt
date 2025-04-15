package com.jam.chatz.start.signin

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jam.chatz.databinding.ActivitySignInScreenBinding
import com.jam.chatz.start.home.Home
import com.jam.chatz.start.signup.SignUpScreen

class SignInScreen : AppCompatActivity() {
    private val binding: ActivitySignInScreenBinding by lazy {
        ActivitySignInScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        alreadyLogin()
        var auth: FirebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        binding.regbtn.setOnClickListener { startActivity(Intent(this, SignUpScreen::class.java)) }
        binding.lgbtn.setOnClickListener {
            var email = binding.email.text.toString()
            var pass = binding.pass.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please fill all the fields & Check your connection",
                    Toast.LENGTH_LONG
                ).show()
            } else if (!isInternetAvailable()) {
                Toast.makeText(this, "Please check your Internet Connection", Toast.LENGTH_LONG).show()
            } else {
                binding.pgbar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Logged-in successfully", Toast.LENGTH_LONG).show()
                        finish()
                        binding.pgbar.visibility = View.GONE

                        startActivity(Intent(this, Home::class.java))
                    } else {
                        binding.pgbar.visibility = View.GONE
                        Toast.makeText(
                            this, "Login failed ${task.exception.toString()}", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(touch: MotionEvent): Boolean {
        if (touch.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(touch.rawX.toInt(), touch.rawY.toInt())) {
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
        }
        return super.dispatchTouchEvent(touch)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun alreadyLogin() {
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}