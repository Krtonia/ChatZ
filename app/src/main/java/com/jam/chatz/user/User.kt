package com.jam.chatz.user

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class User(
    val userid: String? = "",
    val username: String? = "",
    val useremail: String? = "",
    val status: String? = "",
    val imageurl: String? = "",
    val lastMessage: String? = "",
    var lastMessageTimestamp: Timestamp? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userid)
        parcel.writeString(username)
        parcel.writeString(status)
        parcel.writeString(useremail)
        parcel.writeString(imageurl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}