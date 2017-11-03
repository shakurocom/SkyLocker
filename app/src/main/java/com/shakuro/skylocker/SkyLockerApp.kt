package com.shakuro.skylocker

import android.app.Application
import com.shakuro.skylocker.di.ComponentsManager

class SkyLockerApp: Application() {
    lateinit var componentsManager: ComponentsManager

    override fun onCreate() {
        super.onCreate()
        componentsManager = ComponentsManager(this)
    }
}
