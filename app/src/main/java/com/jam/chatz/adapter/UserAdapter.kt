package com.jam.chatz.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jam.chatz.R
import com.jam.chatz.user.User

class UserAdapter(
    private var users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        val userImageView: ImageView = itemView.findViewById(R.id.user_image_view)
        val lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users[position]
        holder.usernameTextView.text = currentUser.username
        holder.lastMessageText.text = currentUser.lastMessage ?: ""
        Glide.with(holder.itemView.context)
            .load(currentUser.imageurl)
            .placeholder(R.drawable.img)
            .error(R.drawable.img)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .into(holder.userImageView)

        holder.itemView.setOnClickListener {
            onUserClick(currentUser)
        }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers.sortedByDescending { it.lastMessageTimestamp?.seconds ?: 0 }
        notifyDataSetChanged()
    }
}