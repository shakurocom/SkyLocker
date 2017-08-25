package com.shakuro.skylocker

import android.app.Activity
import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.*
import android.view.WindowManager.LayoutParams
import android.widget.TextView
import com.shakuro.skylocker.lock.LockscreenService
import com.shakuro.skylocker.lock.LockscreenUtils
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.entities.Meaning
import kotlinx.android.synthetic.main.activity_lockscreen.*
import java.io.BufferedReader
import java.io.InputStreamReader


class LockScreenActivity : Activity(), LockscreenUtils.OnLockStatusChangedListener {
    private var lockscreenUtils: LockscreenUtils? = null
    private var currentMeaning: Meaning? = null

    companion object {
        private const val DRAG_VIEW_TAG = "DRAG_VIEW_TAG"
    }

    // Set appropriate flags to make the screen appear over the keyguard
    override fun onAttachedToWindow() {
        this.window.setType(LayoutParams.TYPE_KEYGUARD_DIALOG)
        this.window.addFlags(LayoutParams.FLAG_FULLSCREEN or
                LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                LayoutParams.FLAG_KEEP_SCREEN_ON or
                LayoutParams.FLAG_DISMISS_KEYGUARD)
        super.onAttachedToWindow()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lockscreen)

        init()

        // unlock screen in case of app get killed by system
        if (intent != null && intent.hasExtra("kill") && intent.extras!!.getInt("kill") == 1) {
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
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            } catch (e: Exception) {
                println("error: $e")
            }
        }
    }

    private fun init() {
        SkyLockerManager.initInstance(this)
        lockscreenUtils = LockscreenUtils()

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
                unlockHomeButton()
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
                unlockHomeButton()
            }, delay)
        }
    }

    private fun words(): List<String> {
        val stream = resources.openRawResource(R.raw.words)
        val rdr = BufferedReader(InputStreamReader(stream))
        var line = rdr.readLine()
        val words = mutableListOf<String>()
        while (line != null) {
            line = rdr.readLine()
            if ((line?.length ?: 0) > 0) {
                words.add(line)
            }
        }
        rdr.close()
        stream.close()
        return words
    }

    // Handle events of calls and unlock screen if necessary
    private inner class StateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {

            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> unlockHomeButton()
                TelephonyManager.CALL_STATE_OFFHOOK -> { }
                TelephonyManager.CALL_STATE_IDLE -> { }
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
        lockscreenUtils?.lock(this)
    }

    // Unlock home button and wait for its callback
    fun unlockHomeButton() {
        lockscreenUtils?.unlock()
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