package com.shakuro.skylocker.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shakuro.skylocker.model.SkyLockerManager


class BootCompletedIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        SkyLockerManager.initInstance(context)
        if (SkyLockerManager.instance.lockingEnabled && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startService(Intent(context, LockscreenService::class.java))
        }
    }
}
