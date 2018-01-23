package com.shakuro.skylocker.system.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shakuro.skylocker.di.Scopes
import com.shakuro.skylocker.model.settings.SettingsRepository
import toothpick.Toothpick
import javax.inject.Inject

class BootCompletedIntentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val appScope = Toothpick.openScope(Scopes.APP_SCOPE)
        Toothpick.inject(this, appScope)

        if (settingsRepository.lockingEnabled && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startService(Intent(context, LockscreenService::class.java))
        }
    }
}
