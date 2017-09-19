package com.shakuro.skylocker.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.shakuro.skylocker.LockScreenActivity
import com.shakuro.skylocker.model.SkyLockerManager
import java.util.*
import java.util.concurrent.TimeUnit


class LockscreenIntentReceiver : BroadcastReceiver() {

    // Handle actions and display Lockscreen
    override fun onReceive(context: Context, intent: Intent) {
        SkyLockerManager.initInstance(context)
        if (SkyLockerManager.instance.lockingEnabled &&
                (intent.action == Intent.ACTION_SCREEN_ON || intent.action == Intent.ACTION_BOOT_COMPLETED)) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager.callState == TelephonyManager.CALL_STATE_IDLE) {
                startLockscreen(context)
            }
        }
    }

    // Display lock screen
    private fun startLockscreen(context: Context) {
        val mIntent = Intent(context, LockScreenActivity::class.java)
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(mIntent)
    }
}
