package com.shakuro.skylocker

import android.app.Application
import com.shakuro.skylocker.di.AppComponent
import com.shakuro.skylocker.di.DaggerAppComponent
import com.shakuro.skylocker.di.modules.ContextModule


class SkyLockerApp: Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().contextModule(ContextModule(this)).build()
        appComponent.inject(this)
    }
}
