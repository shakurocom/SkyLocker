package com.shakuro.skylocker

import android.app.Application
import com.shakuro.skylocker.di.Scopes
import com.shakuro.skylocker.di.application.AppModule
import com.shakuro.skylocker.di.application.SkyEngModule
import toothpick.Toothpick

class SkyLockerApp: Application() {

    override fun onCreate() {
        super.onCreate()

        Toothpick.openScope(Scopes.APP_SCOPE)
                .installModules(AppModule(this))

        Toothpick.openScopes(Scopes.APP_SCOPE, Scopes.SKY_ENG_SCOPE)
                .installModules(SkyEngModule())
    }
}
