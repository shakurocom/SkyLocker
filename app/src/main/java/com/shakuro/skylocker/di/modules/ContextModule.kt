package com.shakuro.skylocker.di.modules

import android.content.Context
import com.shakuro.skylocker.SkyLockerApp

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier
annotation class ForApplication

@Module
class ContextModule(private val application: SkyLockerApp) {

    @Provides
    @Singleton
    @ForApplication
    fun provideApplicationContext(): Context {
        return application
    }
}
