package com.shakuro.skylocker.system.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.shakuro.skylocker.di.Scopes
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.presentation.quiz.QuizActivity
import toothpick.Toothpick
import javax.inject.Inject

class LockscreenIntentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    // Handle actions and display Lockscreen
    override fun onReceive(context: Context, intent: Intent) {
        val appScope = Toothpick.openScope(Scopes.APP_SCOPE)
        Toothpick.inject(this, appScope)

        if (settingsRepository.lockingEnabled && intent.action == Intent.ACTION_SCREEN_ON) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager.callState == TelephonyManager.CALL_STATE_IDLE) {
                startLockscreen(context)
            }
        }
    }

    // Display lock screen
    private fun startLockscreen(context: Context) {
        val intent = Intent(context, QuizActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
