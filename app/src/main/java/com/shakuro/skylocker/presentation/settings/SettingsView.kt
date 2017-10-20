package com.shakuro.skylocker.presentation.settings

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface SettingsView : MvpView {

    fun setUserAuthorized(authorized: Boolean)

    fun setUserEmail(email: String)

    fun setLockEnabled(enabled: Boolean)

    fun setUseTop1000Words(use: Boolean)

    fun setUseUserWords(use: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgressDialog(message: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun hideProgressDialog()

    @StateStrategyType(SkipStrategy::class)
    fun showMessage(message: String)

    @StateStrategyType(SkipStrategy::class)
    fun showError(error: String)
}