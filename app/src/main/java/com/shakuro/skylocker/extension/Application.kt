package com.shakuro.skylocker.extension

import android.app.Application
import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.di.AppComponent

val Application.appComponent: AppComponent
    get() = (this as SkyLockerApp).appComponent