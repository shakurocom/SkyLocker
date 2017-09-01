package com.shakuro.skylocker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.TextView
import com.shakuro.skylocker.lock.LockscreenService
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.entities.Meaning
import kotlinx.android.synthetic.main.activity_lockscreen.*


class LockScreenActivity : Activity() {
    private var currentMeaning: Meaning? = null

    // Set appropriate flags to make the screen appear over the keyguard
    override fun onAttachedToWindow() {
        this.window.addFlags(LayoutParams.FLAG_FULLSCREEN or
                LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                LayoutParams.FLAG_KEEP_SCREEN_ON or
                LayoutParams.FLAG_DISMISS_KEYGUARD)
        this.window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onAttachedToWindow()
    }
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lockscreen)

        init()

        // unlock screen in case of app get killed by system
        if (intent != null && intent.hasExtra("kill") && intent.extras!!.getInt("kill") == 1) {
            unlockDevice()
        } else {
            try {
                // start service for observing intents
                startService(Intent(this, LockscreenService::class.java))

                // listen the events get fired during the call
                val phoneStateListener = StateListener()
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            } catch (e: Exception) {
                println("error: $e")
            }
        }
    }

    private fun init() {
        SkyLockerManager.initInstance(this)

        showRandomMeaning()
        SkyLockerManager.instance.refreshUserMeaningsInBackground()

        val bgImage = SkyLockerManager.instance.getBlurredBgImage(this)
        bgImage?.let {
            backgroundImageView.setImageBitmap(bgImage)
        }
    }

    private fun showRandomMeaning(): Meaning? {
        currentMeaning = null
        this.flowLayout.removeAllViews()

        currentMeaning = SkyLockerManager.instance.randomMeaning()
        currentMeaning?.let {
            wordTextView.setText(it.translation)
            definitionTextView.setText(it.definition)

            val answers = SkyLockerManager.instance.answerWithAlternatives(it)

            val answerClickListener = View.OnClickListener { v ->
                checkAnswer(v)
            }

            answers.forEach {
                val answerTextView = LayoutInflater.from(this.flowLayout.context).inflate(R.layout.answer_textview, this.flowLayout, false) as TextView
                answerTextView.setText(it)
                this.flowLayout.addView(answerTextView)
                answerTextView.setOnClickListener(answerClickListener)
            }
        }

        skipQuizView.setOnClickListener {
            unlockDevice()
        }

        return currentMeaning
    }

    private fun checkAnswer(v: View?) {
        if (v != null && v is TextView) {
            val correctAnswer = v.text == currentMeaning?.text

            val delay = if (correctAnswer) 500L else 1000L
            val answerBackground = if (correctAnswer) R.drawable.correct_answer_bg else R.drawable.wrond_answer_bg
            val answerColor = ContextCompat.getColor(this, if (correctAnswer) R.color.correct_answer else R.color.wrong_answer)
            v.setBackgroundResource(answerBackground)
            v.setTextColor(answerColor)

            Handler().postDelayed({
                unlockDevice()
            }, delay)
        }
    }

    // Handle events of calls and unlock screen if necessary
    private inner class StateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {

            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> unlockDevice()
                TelephonyManager.CALL_STATE_OFFHOOK -> { }
                TelephonyManager.CALL_STATE_IDLE -> { }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        unlockDevice()
    }

    //Simply unlock device by finishing the activity
    private fun unlockDevice() {
        finish()
    }
}