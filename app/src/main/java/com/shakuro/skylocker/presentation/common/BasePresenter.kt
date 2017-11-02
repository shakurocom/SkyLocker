package com.shakuro.skylocker.presentation.common

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<T : MvpView> : MvpPresenter<T>() {

    protected val disposeOnDestroy = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        disposeOnDestroy.clear()
    }
}
