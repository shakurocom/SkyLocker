package com.shakuro.skylocker.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.shakuro.skylocker.LockScreenActivity


class LockscreenIntentReceiver : BroadcastReceiver() {

    // Handle actions and display Lockscreen
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_SCREEN_OFF
                || intent.action == Intent.ACTION_SCREEN_ON
                || intent.action == Intent.ACTION_BOOT_COMPLETED) {
            start_lockscreen(context)
        }

    }

    // Display lock screen
    private fun start_lockscreen(context: Context) {
        val mIntent = Intent(context, LockScreenActivity::class.java)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(mIntent)
    }

}
