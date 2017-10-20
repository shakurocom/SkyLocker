package com.shakuro.skylocker.presentation.settings

import android.util.Patterns
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.SkyLockerManager
import ru.terrakok.gitlabclient.model.system.LockServiceManager
import ru.terrakok.gitlabclient.model.system.ResourceManager
import javax.inject.Inject

@InjectViewState
class SettingsPresenter : MvpPresenter<SettingsView>() {

    @Inject
    lateinit var skyLockerManager: SkyLockerManager

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var lockServiceManager: LockServiceManager

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.setLockEnabled(lockServiceManager.isLockServiceActive())

        if (skyLockerManager.lockingEnabled && !lockServiceManager.isLockServiceActive()) {
            lockServiceManager.startLockService()
        }
    }

    fun onConnectAction(email: String?, token: String?) {
        if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewState.showError(getString(R.string.invalid_e_mail))
            return
        }

        if (token == null || token.trim().isEmpty()) {
            viewState.showError(getString(R.string.token_is_empty))
            return
        }

        viewState.showProgressDialog(getString(R.string.connecting_skyeng))
        skyLockerManager.refreshUserMeanings(email, token, { _, error ->
            viewState.hideProgressDialog()
            if (error == null) {
                refresAuthorizedState()
            } else {
                viewState.showError(error.localizedMessage)
            }
        })
    }

    fun onDisconnectAction() {
        skyLockerManager.disconnectActiveUser()
        refresAuthorizedState()
        checkWordsExist()
    }

    fun onRequestTokenAction(email: String?) {
        if (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewState.showProgressDialog(getString(R.string.requesting_token))
            skyLockerManager.requestToken(email, { error ->
                viewState.hideProgressDialog()
                if (error == null) {
                    viewState.showMessage(getString(R.string.token_requested))
                } else {
                    viewState.showError(error.localizedMessage)
                }
            })
        } else {
            viewState.showError(getString(R.string.invalid_e_mail))
        }
    }

    fun onLockChangedAction(locked: Boolean) {
        if (locked) {
            lockServiceManager.startLockService()
        } else {
            lockServiceManager.stopLockService()
        }
    }

    fun onUseTop1000WordsAction(use: Boolean) {
        skyLockerManager.useTop1000Words = use
        checkWordsExist()
    }

    fun onUseUserWordsAction(use: Boolean) {
        skyLockerManager.useUserWords = use
        checkWordsExist()
    }

    private fun checkWordsExist() {
        with (skyLockerManager) {
            if (lockingEnabled) {
                if (randomMeaning() == null) {
                    lockServiceManager.stopLockService()
                    viewState.setLockEnabled(false)
                    viewState.showMessage(getString(R.string.no_words_for_studying))
                } else {
                    if (useUserWords || useTop1000Words) {
                        lockServiceManager.startLockService()
                        viewState.setLockEnabled(true)
                    }
                }
            }
        }
    }

    private fun getString(id: Int) = resourceManager.getString(id)

    private fun refresAuthorizedState() {
        val user = skyLockerManager.activeUser()
        viewState.setUserAuthorized(user != null)
        user?.let {
            viewState.setUserEmail(user.email)
        }
    }
}