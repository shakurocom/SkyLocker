package com.shakuro.skylocker.presentation.settings

import com.arellomobile.mvp.InjectViewState
import com.shakuro.skylocker.R
import com.shakuro.skylocker.extension.addTo
import com.shakuro.skylocker.model.settings.SettingsInteractor
import com.shakuro.skylocker.presentation.common.BasePresenter
import com.shakuro.skylocker.system.SchedulersProvider
import ru.terrakok.gitlabclient.model.system.ResourceManager
import javax.inject.Inject

@InjectViewState
class SettingsPresenter @Inject constructor(val settingsInteractor: SettingsInteractor,
                                            val resourceManager: ResourceManager,
                                            val schedulersProvider: SchedulersProvider) : BasePresenter<SettingsView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.setLockEnabled(settingsInteractor.lockingEnabled)
        viewState.setUseTop1000Words(settingsInteractor.useTop1000Words)
        viewState.setUseUserWords(settingsInteractor.useUserWords)
        refreshConnectedState()

        settingsInteractor.noQuizesObservable
                .observeOn(schedulersProvider.ui())
                .subscribe( { viewState.showMessage(getString(R.string.no_words_for_studying)) },
                        { error -> showError(error) })
                .addTo(disposeOnDestroy)

        settingsInteractor.lockChangedObservable
                .observeOn(schedulersProvider.ui())
                .subscribe({ lock -> viewState.setLockEnabled(lock) },
                        { error -> showError(error) })
                .addTo(disposeOnDestroy)
    }

    fun onLockChangedAction(locked: Boolean) {
        settingsInteractor.lockingEnabled = locked
    }

    fun onConnectAction(email: String?, token: String?) {
        settingsInteractor.connectUser(email, token)
                .observeOn(schedulersProvider.ui())
                .doOnSubscribe { viewState.showProgressDialog(getString(R.string.connecting_skyeng)) }
                .doAfterTerminate { viewState.hideProgressDialog() }
                .subscribe( { refreshConnectedState() },
                        { error -> showError(error) })
                .addTo(disposeOnDestroy)
    }

    fun onDisconnectAction() {
        settingsInteractor.disconnectUser()
                .observeOn(schedulersProvider.ui())
                .doOnSubscribe { viewState.showProgressDialog(getString(R.string.disconnecting_skyeng)) }
                .doAfterTerminate { viewState.hideProgressDialog() }
                .subscribe({ refreshConnectedState() },
                        { error -> showError(error) })
                .addTo(disposeOnDestroy)
    }

    fun onRequestTokenAction(email: String?) {
        settingsInteractor.requestToken(email)
                .observeOn(schedulersProvider.ui())
                .doOnSubscribe { viewState.showProgressDialog(getString(R.string.requesting_token)) }
                .doAfterTerminate { viewState.hideProgressDialog() }
                .subscribe({ viewState.showMessage(getString(R.string.token_requested)) },
                        { error -> showError(error) })
                .addTo(disposeOnDestroy)
    }

    fun onUseTop1000WordsAction(use: Boolean) {
        settingsInteractor.useTop1000Words = use
    }

    fun onUseUserWordsAction(use: Boolean) {
        settingsInteractor.useUserWords = use
    }

    private fun showError(error: Throwable) {
        viewState.showError(error.localizedMessage)
    }

    private fun getString(id: Int) = resourceManager.getString(id)

    private fun refreshConnectedState() {
        val connected = settingsInteractor.connected
        viewState.setUserAuthorized(connected)
        if (connected) {
            viewState.setUserEmail(settingsInteractor.email)
        }
    }
}