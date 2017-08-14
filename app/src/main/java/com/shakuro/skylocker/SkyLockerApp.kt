package com.shakuro.skylocker

import android.app.Application

import com.shakuro.skylocker.model.SkyLockerManager

class SkyLockerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SkyLockerManager.initInstance(this)
    }
}
