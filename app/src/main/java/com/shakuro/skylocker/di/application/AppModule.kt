package com.shakuro.skylocker.di.application

import android.content.Context
import android.content.SharedPreferences
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.system.AppSchedulers
import com.shakuro.skylocker.system.LockServiceManager
import com.shakuro.skylocker.system.RingStateManager
import com.shakuro.skylocker.system.SchedulersProvider
import ru.terrakok.gitlabclient.model.system.ResourceManager
import toothpick.config.Module

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)
        bind(ResourceManager::class.java).singletonInScope()
        bind(RingStateManager::class.java).singletonInScope()
        bind(LockServiceManager::class.java).singletonInScope()
        bind(SettingsRepository::class.java).singletonInScope()
        bind(SchedulersProvider::class.java).toInstance(AppSchedulers())

        bind(SharedPreferences::class.java)
                .toInstance(context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE))
    }
}