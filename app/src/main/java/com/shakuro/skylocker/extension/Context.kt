package com.shakuro.skylocker.extension

import android.content.Context
import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.di.ComponentsManager

val Context.componentsManager: ComponentsManager
    get() = (this.applicationContext as SkyLockerApp).componentsManager