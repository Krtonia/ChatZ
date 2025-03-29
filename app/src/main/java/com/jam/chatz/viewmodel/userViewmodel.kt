package com.jam.chatz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jam.chatz.user.User
import com.jam.chatz.user.UserRepo

class UserViewModel : ViewModel() {
    private val userRepo = UserRepo()
    val users: LiveData<List<User>> = userRepo.getUsers()
}