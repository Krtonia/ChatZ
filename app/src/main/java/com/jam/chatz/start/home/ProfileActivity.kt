package com.jam.chatz.start.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jam.chatz.R
import com.jam.chatz.cloudinary.Uploader
import com.jam.chatz.databinding.ActivityProfileBinding
import com.jam.chatz.start.signin.SignInScreen
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var isUploadingImage = false

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            uploadProfileImage(selectedUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logoutBtn.setOnClickListener { showLogoutConfirmation() }
        loadUserProfile()
        binding.changeUname.setOnClickListener { openDialog() }
        binding.changeStatus.setOnClickListener { statusDialog() }
        binding.imgedit.setOnClickListener {
            if (!isUploadingImage) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Please wait, image is uploading...", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun openImagePicker() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Change Profile Picture")
            .setMessage("Select a new profile picture from your gallery")
            .setPositiveButton("Choose from Gallery") { _, _ ->
                photoPickerLauncher.launch("image/*")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadProfileImage(uri: Uri) {
        if (isUploadingImage) return

        isUploadingImage = true

        binding.imgedit.isEnabled = false
        Toast.makeText(this, "Uploading profile picture...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val imageUrl = Uploader.uploadImage(this@ProfileActivity, uri)

                if (imageUrl != null) {
                    updateProfileImageInFirebase(imageUrl)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Failed to upload image. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Error uploading image: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isUploadingImage = false
                runOnUiThread {
                    binding.imgedit.isEnabled = true
                }
            }
        }
    }

    private fun updateProfileImageInFirebase(imageUrl: String) {
        val currentUser = auth.currentUser ?: run {
            startActivity(Intent(this, SignInScreen::class.java))
            finish()
            return
        }

        val userData = hashMapOf(
            "imageurl" to imageUrl
        )

        db.collection("Users")
            .document(currentUser.uid)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                @GlideModule
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.img)
                    .into(binding.profileimg)

                Toast.makeText(this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile picture: ${e.message}", Toast.LENGTH_LONG).show()
            }
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

        db.collection("Users").document(currentUser.uid).get().addOnSuccessListener { document ->
            if (document != null && document.contains("imageurl")) {
                val imageUrl = document.getString("imageurl")
                if (!imageUrl.isNullOrEmpty()) {
                    @GlideModule
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