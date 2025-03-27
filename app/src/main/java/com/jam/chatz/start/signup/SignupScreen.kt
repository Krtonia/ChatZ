package com.jam.chatz.start.signup

import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jam.chatz.databinding.ActivitySignupScreenBinding
import com.jam.chatz.start.Home.Home
import com.jam.chatz.start.signin.SignInScreen

class SignUpScreen : AppCompatActivity() {

    private val binding : ActivitySignupScreenBinding by lazy {
        ActivitySignupScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

         var auth : FirebaseAuth
         auth = FirebaseAuth.getInstance()

        binding.signupbutton.setOnClickListener {

            //Getting User Information
            val name = binding.signupname.text.trim().toString()
            val email = binding.signupemail.text.trim().toString()
            val pass = binding.signuppassword.text.trim().toString()
            val cnfpass = binding.signuconfirmpassword.text.trim().toString()

            //Check if all fields are fillled
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || cnfpass.isEmpty()){
                Toast.makeText(this,"Please Fill all required details", Toast.LENGTH_LONG).show()
            }
            else if (pass != cnfpass)
            {
                Toast.makeText(this,"Password and confirm password are not same", Toast.LENGTH_LONG).show()
            }
            else
            {
                auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener { task ->
                    if(task.isSuccessful)
                    {
                        Toast.makeText(this,"Registration is successfull", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, SignInScreen::class.java))
                    }
                    else
                    {
                        Toast.makeText(this,"Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}