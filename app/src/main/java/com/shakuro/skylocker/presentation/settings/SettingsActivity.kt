package com.shakuro.skylocker.presentation.settings

import android.app.ProgressDialog
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SwitchCompat
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.shakuro.skylocker.R
import com.shakuro.skylocker.appComponent
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : MvpAppCompatActivity(), SettingsView {
    private var progressDialog: ProgressDialog? = null
    private var lockingSwitch: SwitchCompat? = null

    @InjectPresenter
    lateinit var presenter: SettingsPresenter

    @ProvidePresenter
    fun providePresenter(): SettingsPresenter {
        val presenter = SettingsPresenter()
        application.appComponent.inject(presenter)
        return presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        connectButton.setOnClickListener {
            presenter.onConnectAction(emailEditText.text?.toString(), tokenEditText.text?.toString())
        }

        disconnectButton.setOnClickListener {
            presenter.onDisconnectAction()
        }

        resendTokenButton.setOnClickListener {
            presenter.onRequestTokenAction(emailEditText.text?.toString())
        }

        top1000WordsCheckBox.setOnCheckedChangeListener { _, use ->
            presenter.onUseTop1000WordsAction(use)
        }

        skyEngUserWordsCheckBox.setOnCheckedChangeListener { _, use ->
            presenter.onUseUserWordsAction(use)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)

        val menuItem = menu.findItem(R.id.switchItem)
        val view = MenuItemCompat.getActionView(menuItem)
        lockingSwitch = view.findViewById(R.id.switchForActionBar) as SwitchCompat
        lockingSwitch?.setOnCheckedChangeListener { _, checked ->
             presenter.onLockChangedAction(checked)
        }

        presenter.requestLockStateUpdate()
        return true
    }

    override fun setUserAuthorized(authorized: Boolean) {
        // show / hide controls depending on active user exists
        val USER_AVAILABLE_TAG = getString(R.string.user_authorized)
        val USER_NOT_AVAILABLE_TAG = getString(R.string.user_not_authorized)

        // change visibility
        (0..settingsConstraintLayout.childCount - 1)
                .map { settingsConstraintLayout.getChildAt(it) }
                .forEach {
                    when (it.tag) {
                        USER_AVAILABLE_TAG -> it.visibility = if (authorized) View.VISIBLE else View.GONE
                        USER_NOT_AVAILABLE_TAG -> it.visibility = if (authorized) View.GONE else View.VISIBLE
                    }
                }

        // change constraints
        val constraintSet = ConstraintSet()
        constraintSet.clone(settingsConstraintLayout)
        val separatorConnectViewId = if (authorized) disconnectButton.id else connectButton.id
        constraintSet.connect(connectSeparator.id, ConstraintSet.TOP, separatorConnectViewId, ConstraintSet.BOTTOM)
        constraintSet.applyTo(settingsConstraintLayout)

        if (!authorized) {
            userTextView.text = getString(R.string.connect_skyeng)
        }
    }

    override fun setUserEmail(email: String) {
        userTextView.text = email
    }

    override fun setLockEnabled(enabled: Boolean) {
        lockingSwitch?.isChecked = enabled
    }

    override fun setUseTop1000Words(use: Boolean) {
        top1000WordsCheckBox.isChecked = use
    }

    override fun setUseUserWords(use: Boolean) {
        skyEngUserWordsCheckBox.isChecked = use
    }

    override fun showProgressDialog(message: String) {
        if (progressDialog?.window == null) {
            progressDialog = ProgressDialog(this, ProgressDialog.STYLE_SPINNER)
        }
        progressDialog?.let {
            it.setCancelable(false)
            it.setMessage(message)
            if (!it.isShowing) {
                it.show()
            }
        }
    }

    override fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing && it.window != null) {
                it.dismiss()
            }
        }
        progressDialog = null
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}