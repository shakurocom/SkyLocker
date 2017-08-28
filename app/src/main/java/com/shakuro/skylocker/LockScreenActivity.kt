package com.shakuro.skylocker

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.shakuro.skylocker.lock.LockscreenService
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.entities.Meaning
import kotlinx.android.synthetic.main.activity_lockscreen.*


class LockScreenActivity : Activity() {
    private var currentMeaning: Meaning? = null

    companion object {
        private const val DRAG_VIEW_TAG = "DRAG_VIEW_TAG"
    }

    // Set appropriate flags to make the screen appear over the keyguard
    override fun onAttachedToWindow() {
        this.window.addFlags(LayoutParams.FLAG_FULLSCREEN or
                LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                LayoutParams.FLAG_KEEP_SCREEN_ON or
                LayoutParams.FLAG_DISMISS_KEYGUARD)
        this.window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onAttachedToWindow()
    }

    override fun onResume() {
        super.onResume()

        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
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

        root.setDragEventListener {
            when (it.action) {
                DragEvent.ACTION_DROP -> {
                    lockView.visibility = View.VISIBLE
                }
            }
        }

        skipQuizView.setOnDragListener { v, event ->
            if (event?.action == DragEvent.ACTION_DROP) {
                lockView.visibility = View.INVISIBLE
                unlockDevice()
            }
            true
        }

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

            val answerDragListener = View.OnDragListener { v, event ->
                if (event?.action == DragEvent.ACTION_DROP) {
                    lockView.visibility = View.INVISIBLE
                    checkAnswer(v)
                }
                true
            }

            answers.forEach {
                val answerTextView = LayoutInflater.from(this.flowLayout.context).inflate(R.layout.answer_textview, this.flowLayout, false) as TextView
                answerTextView.setText(it)
                this.flowLayout.addView(answerTextView)
                answerTextView.setOnDragListener(answerDragListener)
            }
        }

        lockView.tag = DRAG_VIEW_TAG

        lockView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lockView.visibility = View.INVISIBLE
                    val item = ClipData.Item(v.tag as CharSequence)
                    // Create a new ClipData using the tag as a label, the plain
                    // text MIME type, and
                    // the already-created item. This will create a new
                    // ClipDescription object within the
                    // ClipData, and set its MIME type entry to "text/plain"
                    val dragData = ClipData(v.tag as CharSequence,
                            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                            item)
                    // DragContainer has its own DragShadowBuilder
                    root.startDragChild(v, dragData, // the data to be dragged
                            null, // no need to use local data
                            0 // flags (not currently used, set to 0)
                    )
                }
            }
            true
        }

        return currentMeaning
    }

    private fun checkAnswer(v: View?) {
        if (v != null && v is TextView) {
            val correctAnswer = v.text == currentMeaning?.text

            val delay = if (correctAnswer) 500L else 1000L
            val answerBackground = if (correctAnswer) R.drawable.correct_answer_bg else R.drawable.wrond_answer_bg
            v.setBackgroundResource(answerBackground)

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

    // Don't finish Activity on Back press
    override fun onBackPressed() {
        super.onBackPressed()
        unlockDevice()
    }

    //Simply unlock device by finishing the activity
    private fun unlockDevice() {
        finish()
    }
}