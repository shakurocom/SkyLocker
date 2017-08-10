package com.shakuro.skylocker

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.WindowManager.LayoutParams
import android.widget.Button

import com.shakuro.skylocker.lock.LockscreenService
import com.shakuro.skylocker.lock.LockscreenUtils


class LockScreenActivity : Activity(), LockscreenUtils.OnLockStatusChangedListener {

    // User-interface
    private var btnUnlock: Button? = null

    // Member variables
    private var mLockscreenUtils: LockscreenUtils? = null

    // Set appropriate flags to make the screen appear over the keyguard
    override fun onAttachedToWindow() {
        this.window.setType(
                LayoutParams.TYPE_KEYGUARD_DIALOG)
        this.window.addFlags(
                LayoutParams.FLAG_FULLSCREEN or LayoutParams.FLAG_SHOW_WHEN_LOCKED or LayoutParams.FLAG_KEEP_SCREEN_ON or LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        super.onAttachedToWindow()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_lockscreen)

        init()

        // unlock screen in case of app get killed by system
        if (intent != null && intent.hasExtra("kill")
                && intent.extras!!.getInt("kill") == 1) {
            enableKeyguard()
            unlockHomeButton()
        } else {

            try {
                // disable keyguard
                disableKeyguard()

                // lock home button
                lockHomeButton()

                // start service for observing intents
                startService(Intent(this, LockscreenService::class.java))

                // listen the events get fired during the call
                val phoneStateListener = StateListener()
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                telephonyManager.listen(phoneStateListener,
                        PhoneStateListener.LISTEN_CALL_STATE)

            } catch (e: Exception) {
            }

        }
    }

    private fun init() {
        mLockscreenUtils = LockscreenUtils()
        btnUnlock = findViewById(R.id.btnUnlock) as Button
        btnUnlock!!.setOnClickListener {
            // unlock home button and then screen on button press
            unlockHomeButton()
        }
    }

    // Handle events of calls and unlock screen if necessary
    private inner class StateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {

            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> unlockHomeButton()
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                }
            }
        }
    }

    // Don't finish Activity on Back press
    override fun onBackPressed() {
        return
    }

    // Handle button clicks
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_POWER
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {

            return true
        }

        return false

    }

    // handle the key press events here itself
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || event.keyCode == KeyEvent.KEYCODE_POWER) {
            return false
        }
        if (event.keyCode == KeyEvent.KEYCODE_HOME) {

            return true
        }
        return false
    }

    // Lock home button
    fun lockHomeButton() {
        mLockscreenUtils!!.lock(this@LockScreenActivity)
    }

    // Unlock home button and wait for its callback
    fun unlockHomeButton() {
        mLockscreenUtils!!.unlock()
    }

    // Simply unlock device when home button is successfully unlocked
    override fun onLockStatusChanged(isLocked: Boolean) {
        if (!isLocked) {
            unlockDevice()
        }
    }

    override fun onStop() {
        super.onStop()
        unlockHomeButton()
    }

    private fun disableKeyguard() {
        val mKM = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val mKL = mKM.newKeyguardLock("IN")
        mKL.disableKeyguard()
    }

    private fun enableKeyguard() {
        val mKM = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val mKL = mKM.newKeyguardLock("IN")
        mKL.reenableKeyguard()
    }

    //Simply unlock device by finishing the activity
    private fun unlockDevice() {
        finish()
    }

}