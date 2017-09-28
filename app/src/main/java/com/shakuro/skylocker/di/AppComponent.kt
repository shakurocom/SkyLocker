package com.shakuro.skylocker.di

import com.shakuro.skylocker.LockScreenActivity
import com.shakuro.skylocker.SettingsActivity
import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.di.modules.BlurredImageModule
import com.shakuro.skylocker.di.modules.SkyLockerManagerModule
import com.shakuro.skylocker.lock.BootCompletedIntentReceiver
import com.shakuro.skylocker.lock.LockscreenIntentReceiver
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(SkyLockerManagerModule::class, BlurredImageModule::class))
interface AppComponent {
    fun inject(application: SkyLockerApp)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(lockScreenActivity: LockScreenActivity)
    fun inject(bootCompletedIntentReceiver: BootCompletedIntentReceiver)
    fun inject(lockscreenIntentReceiver: LockscreenIntentReceiver)
}