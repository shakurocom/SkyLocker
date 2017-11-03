package com.shakuro.skylocker.di.application

import android.content.Context
import android.content.SharedPreferences
import com.shakuro.skylocker.di.application.ApplicationContext
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.system.LockServiceManager
import com.shakuro.skylocker.system.RingStateManager
import dagger.Module
import dagger.Provides
import ru.terrakok.gitlabclient.model.system.ResourceManager
import javax.inject.Singleton

@Module
class ApplicationModule(private val appContext: Context) {

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(): Context {
        return appContext
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences {
        return appContext.getSharedPreferences(appContext.packageName, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideResourceManager(): ResourceManager {
        return ResourceManager(appContext)
    }

    @Provides
    @Singleton
    fun provideRingStateManager(): RingStateManager {
        return RingStateManager(appContext)
    }

    @Provides
    @Singleton
    fun provideLockServiceManager() = LockServiceManager(appContext)

    @Provides
    @Singleton
    fun provideSettingsRepository(preferences: SharedPreferences) = SettingsRepository(preferences)
}
