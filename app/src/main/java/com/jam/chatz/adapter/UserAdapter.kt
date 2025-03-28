package com.jam.chatz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jam.chatz.R
import com.jam.chatz.User

class UserAdapter(private var users: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // ViewHolder class
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        val emailTextView: TextView = itemView.findViewById(R.id.email_text_view)
        val userImageView: ImageView = itemView.findViewById(R.id.user_image_view)
        val statusTextView: TextView = itemView.findViewById(R.id.status_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users[position]
        holder.usernameTextView.text = currentUser.username ?: "No name"
        holder.emailTextView.text = currentUser.useremail ?: "No email"
        holder.statusTextView.text = currentUser.status ?: "Offline"

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(currentUser.imageurl ?: R.drawable.img)
            .circleCrop()
            .into(holder.userImageView)
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}