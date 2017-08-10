package com.shakuro.skylocker.lock

import android.app.Activity
import android.app.AlertDialog
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager.LayoutParams
import com.shakuro.skylocker.R


class LockscreenUtils {

    // Member variables
    private var mOverlayDialog: OverlayDialog? = null
    private var mLockStatusChangedListener: OnLockStatusChangedListener? = null

    // Interface to communicate with owner activity
    interface OnLockStatusChangedListener {
        fun onLockStatusChanged(isLocked: Boolean)
    }

    // Reset the variables
    init {
        reset()
    }

    // Display overlay dialog with a view to prevent home button click
    fun lock(activity: Activity) {
        if (mOverlayDialog == null) {
            mOverlayDialog = OverlayDialog(activity)
            mOverlayDialog!!.show()
            mLockStatusChangedListener = activity as OnLockStatusChangedListener
        }
    }

    // Reset variables
    fun reset() {
        if (mOverlayDialog != null) {
            mOverlayDialog!!.dismiss()
            mOverlayDialog = null
        }
    }

    // Unlock the home button and give callback to unlock the screen
    fun unlock() {
        if (mOverlayDialog != null) {
            mOverlayDialog!!.dismiss()
            mOverlayDialog = null
            if (mLockStatusChangedListener != null) {
                mLockStatusChangedListener!!.onLockStatusChanged(false)
            }
        }
    }

    // Create overlay dialog for lockedscreen to disable hardware buttons
    private class OverlayDialog(activity: Activity) : AlertDialog(activity, R.style.OverlayDialog) {

        init {
            val params = window!!.attributes
            with(params) {
                type = LayoutParams.TYPE_SYSTEM_ERROR
                dimAmount = 0.0f
                width = 0
                height = 0
                gravity = Gravity.BOTTOM
            }
            window!!.attributes = params
            window!!.setFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED or LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    0xffffff)
            ownerActivity = activity
            setCancelable(false)
        }

        // consume touch events
        override fun dispatchTouchEvent(motionevent: MotionEvent): Boolean {
            return true
        }

    }
}
