package com.shakuro.skylocker.extension

import android.app.Application
import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.di.ComponentsManager

val Application.componentsManager: ComponentsManager
    get() = (this as SkyLockerApp).componentsManager