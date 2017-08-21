package com.shakuro.skylocker

import android.app.Activity
import android.app.KeyguardManager
import android.app.WallpaperManager
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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.shakuro.skylocker.lock.LockscreenService
import com.shakuro.skylocker.lock.LockscreenUtils
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.entities.Meaning
import kotlinx.android.synthetic.main.activity_lockscreen.*
import java.io.BufferedReader
import java.io.InputStreamReader


class LockScreenActivity : Activity(), LockscreenUtils.OnLockStatusChangedListener, View.OnDragListener {
    private var lockscreenUtils: LockscreenUtils? = null
    private var currentMeaning: Meaning? = null

    companion object {
        private const val IS_FIRST_RUN_KEY = "IS_FIRST_RUN_KEY"
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

        val preferences = SkyLockerManager.instance.preferences
        val firstRun = preferences.getBoolean(IS_FIRST_RUN_KEY, true)
        if (firstRun) {
            preferences.edit().putBoolean(IS_FIRST_RUN_KEY, false).apply()
        }

        imageView.setImageBitmap(takeScreenshot())
    }

    private fun takeScreenshot(): Bitmap {
        val logger = TimingLogger("qqqqq")

        val wallpaperManager = WallpaperManager.getInstance(this.applicationContext)
        val drawable = wallpaperManager.drawable

        val width2 = drawable.intrinsicWidth
        val height2 = drawable.intrinsicHeight
        val scale2 = 320.0f / Math.max(width2, height2)
        val newWidth2 = scale2 * width2
        val newHeight2 = scale2 * height2


        val bitmap = Bitmap.createBitmap(newWidth2.toInt(), newHeight2.toInt(), Bitmap.Config.ARGB_8888);
        val canvas = Canvas(bitmap)
//        canvas.scale(newWidth2 / newWidth, newHeight2 / newHeight)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val blurredBitmap = Bitmap.createBitmap(bitmap)

        val rs = RenderScript.create(this)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, bitmap)
        val tmpOut = Allocation.createFromBitmap(rs, blurredBitmap)
        theIntrinsic.setRadius(12.0f)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(blurredBitmap)
        tmpIn.destroy()
        tmpOut.destroy()

        logger.addSplit("qqqqqq")
        logger.dumpToLog()

        return blurredBitmap
    }

    private fun showRandomMeaning(): Meaning? {
        this.flowLayout.removeAllViews()

        currentMeaning = SkyLockerManager.instance.randomMeaning()
        currentMeaning?.let {
            wordTextView.setText(it.translation)
            definitionTextView.setText(it.definition)

            val answers = SkyLockerManager.instance.answerWithAlternatives(it)
            answers.forEach {
                val answerTextView = LayoutInflater.from(this.flowLayout.context).inflate(R.layout.answer_textview, this.flowLayout, false) as TextView
                answerTextView.setText(it)

                this.flowLayout.addView(answerTextView)
                answerTextView.setOnDragListener { v, event ->
                    if (event?.action == DragEvent.ACTION_DROP) {
                        val dropped = event.localState as ImageView
                        if (answerTextView.text == currentMeaning?.text) {
                            dropped.visibility = View.INVISIBLE
                            Handler().postDelayed({
                                unlockHomeButton()
                            }, 500L)
                        }
                    }
                    true
                }
            }
        }

        lockView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
//                    val shadowBuilder = ImageDragShadowBuilder.fromResource(this, R.drawable.ic_lock_with_bg)
                    val  shadowBuilder = View.DragShadowBuilder(v)
                    v.startDrag(null, shadowBuilder, v, 0)
                }
            }
            true
        }

        return currentMeaning
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

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        if (event?.action == DragEvent.ACTION_DROP) {
            val button = v as Button
            val dropped = event.localState as TextView
            if (button.text == currentMeaning?.text) {
                dropped.visibility = View.INVISIBLE
                Handler().postDelayed({
                    unlockHomeButton()
                }, 500L)
            }
        }
        return true
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