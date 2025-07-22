package com.jam.chatz

import android.app.Application
import com.google.android.material.color.DynamicColors

class Chatz: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}