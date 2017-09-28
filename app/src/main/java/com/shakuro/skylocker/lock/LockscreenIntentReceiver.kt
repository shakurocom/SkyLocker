package com.shakuro.skylocker.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.shakuro.skylocker.LockScreenActivity
import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.model.SkyLockerManager
import javax.inject.Inject


class LockscreenIntentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var skyLockerManager: SkyLockerManager

    // Handle actions and display Lockscreen
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as SkyLockerApp).appComponent.inject(this)
        if (skyLockerManager.lockingEnabled && intent.action == Intent.ACTION_SCREEN_ON) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager.callState == TelephonyManager.CALL_STATE_IDLE) {
                startLockscreen(context)
            }
        }
    }

    // Display lock screen
    private fun startLockscreen(context: Context) {
        val intent = Intent(context, LockScreenActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        with(skyLockerManager) {
            locksCount++
            if (shouldRefreshUserMeanings) {
                requestUserMeaningsUpdate()
            }
        }
    }
}
