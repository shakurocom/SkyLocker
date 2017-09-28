package com.shakuro.skylocker.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.model.SkyLockerManager
import javax.inject.Inject


class BootCompletedIntentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var skyLockerManager: SkyLockerManager

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as SkyLockerApp).appComponent.inject(this)
        if (skyLockerManager.lockingEnabled && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startService(Intent(context, LockscreenService::class.java))
        }
    }
}
