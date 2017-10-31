package com.shakuro.skylocker.di.modules

import android.content.Context
import com.shakuro.skylocker.SkyLockerApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val application: SkyLockerApp) {

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(): Context {
        return application
    }
}
