package com.jam.chatz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jam.chatz.User
import com.jam.chatz.UserRepo

class UserViewModel : ViewModel() {
    private val userRepo = UserRepo()
    val users: LiveData<List<User>> = userRepo.getUsers()
}