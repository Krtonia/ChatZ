package com.jam.chatz

import com.google.firebase.auth.FirebaseAuth

class Utils {
    private val auth = FirebaseAuth.getInstance()
    private var userid : String=""

    fun getUiloggedIn(): String{
        if (auth.currentUser!=null)
        {
            userid = auth.currentUser!!.uid
        }

        return userid
    }
}