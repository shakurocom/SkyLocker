package com.shakuro.skylocker

import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.util.Patterns
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.shakuro.skylocker.lock.LockscreenService
import com.shakuro.skylocker.model.SkyLockerManager
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        SkyLockerManager.initInstance(this)
        SkyLockerManager.instance.genBlurredBgImageIfNotExistsAsync(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        resendTokenButton.setOnClickListener { requestToken() }
        buttonConnect.setOnClickListener { connect() }
        buttonDisconnect.setOnClickListener { disconnect() }

        top1000WordsCheckBox.setOnCheckedChangeListener { _, b -> SkyLockerManager.instance.useTop1000Words = b }
        skyEngWordsCheckBox.setOnCheckedChangeListener { _, b -> SkyLockerManager.instance.useUserWords = b }

        refreshUserUI()

        if (SkyLockerManager.instance.lockingEnabled && !isLockscreenServiceActive()) {
            startLockService()
        }
    }

    private fun connect() {
        val email = emailEditText.text?.toString()
        if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_e_mail), Toast.LENGTH_LONG).show()
            return
        }

        val token = tokenEditText.text?.toString()
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.token_is_empty), Toast.LENGTH_LONG).show()
            return
        }

        showProgressDialog(getString(R.string.connecting_skyeng))
        SkyLockerManager.instance.refreshUserMeanings(email, token, { _, error ->
            hideProgressDialog()
            if (error == null) {
                refreshUserUI()
            } else {
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun disconnect() {
        SkyLockerManager.instance.disconnectActiveUser()
        refreshUserUI()
    }

    private fun refreshUserUI() {
        val user = SkyLockerManager.instance.activeUser()
        val userAvailable = user != null

        // show / hide controls depending on active user exists
        val USER_AVAILABLE_TAG = getString(R.string.user_available)
        val USER_NOT_AVAILABLE_TAG = getString(R.string.user_not_available)
        for (childIndex in 0..settingsConstraintLayout.childCount - 1) {
            val child = settingsConstraintLayout.getChildAt(childIndex)
            when (child.tag) {
                USER_AVAILABLE_TAG -> child.visibility = if (userAvailable) View.VISIBLE else View.GONE
                USER_NOT_AVAILABLE_TAG -> child.visibility = if (userAvailable) View.GONE else View.VISIBLE
            }
        }

        // change constraints
        val constraintSet = ConstraintSet()
        constraintSet.clone(settingsConstraintLayout)
        val separatorConnectViewId = if (userAvailable) buttonDisconnect.id else buttonConnect.id
        constraintSet.connect(connectSeparator.id, ConstraintSet.TOP, separatorConnectViewId, ConstraintSet.BOTTOM)
        constraintSet.applyTo(settingsConstraintLayout)

        userTextView.setText(user?.email ?: getString(R.string.connect_skyeng))
        top1000WordsCheckBox.isChecked = SkyLockerManager.instance.useTop1000Words
        skyEngWordsCheckBox.isChecked = SkyLockerManager.instance.useUserWords
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)

        val menuItem = menu.findItem(R.id.switchItem)
        val view = MenuItemCompat.getActionView(menuItem)
        val switch = view.findViewById(R.id.switchForActionBar) as SwitchCompat

        switch.isChecked = SkyLockerManager.instance.lockingEnabled
        switch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                startLockService()
            } else {
                stopLockService()
            }
        }
        return true
    }

    private fun isLockscreenServiceActive(): Boolean {
        var result = false
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if ((LockscreenService::class.java).name == service.service.className) {
                result = true
                break
            }
        }
        return result
    }

    private fun startLockService() {
        SkyLockerManager.instance.lockingEnabled = true
        startService(Intent(this, LockscreenService::class.java))
    }

    private fun stopLockService() {
        SkyLockerManager.instance.lockingEnabled = false
        stopService(Intent(this, LockscreenService::class.java))
    }

    private fun requestToken() {
        val email = emailEditText.text?.toString()
        val validEmail = email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (validEmail) {
            showProgressDialog(getString(R.string.requesting_token))
            SkyLockerManager.instance.requestToken(email!!, { error ->
                hideProgressDialog()
                val message = error?.localizedMessage ?: getString(R.string.token_requested)
                val duration = if (error == null) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                Toast.makeText(this, message, duration).show()
            })
        } else {
            Toast.makeText(this, getString(R.string.invalid_e_mail), Toast.LENGTH_LONG).show()
        }
    }

    private fun showProgressDialog(message: String) {
        if (progressDialog == null || progressDialog?.getWindow() == null || !progressDialog!!.isShowing()) {
            progressDialog = ProgressDialog(this, ProgressDialog.STYLE_SPINNER)
            progressDialog!!.setCancelable(false)
        }
        progressDialog?.setMessage(message)
        if (!progressDialog!!.isShowing()) {
            progressDialog?.show()
        }
    }

    private fun hideProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing() && progressDialog?.getWindow() != null) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }

}