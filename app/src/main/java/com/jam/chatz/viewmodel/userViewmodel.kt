package com.jam.chatz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jam.chatz.user.User
import com.jam.chatz.user.UserRepo

class UserViewModel : ViewModel() {
    private val repo = UserRepo()

    private val _conversationUsers = MutableLiveData<List<User>>()
    val conversationUsers: LiveData<List<User>> = _conversationUsers

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers

    fun loadUsersWithConversations() {
        repo.getUsersWithLastMessage(object : UserRepo.UserRepoCallback {
            override fun onSuccess(users: List<User>) {
                _conversationUsers.postValue(users)
            }
            override fun onFailure(exception: Exception) {
                _conversationUsers.postValue(emptyList())
            }
        })
    }

    fun loadAllUsers() {
        repo.getAllUsers(object : UserRepo.UserRepoCallback {
            override fun onSuccess(users: List<User>) {
                _allUsers.postValue(users)
            }
            override fun onFailure(exception: Exception) {
                _allUsers.postValue(emptyList())
            }
        })
    }
}