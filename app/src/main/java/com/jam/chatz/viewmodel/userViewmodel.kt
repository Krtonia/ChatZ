package com.jam.chatz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jam.chatz.user.User
import com.jam.chatz.user.UserRepo
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repo = UserRepo()
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repo.getUsersWithLastMessage()
        }
    }

}