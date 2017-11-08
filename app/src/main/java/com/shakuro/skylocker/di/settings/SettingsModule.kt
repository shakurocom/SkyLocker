package com.shakuro.skylocker.di.settings

import com.shakuro.skylocker.model.settings.SettingsInteractor
import com.shakuro.skylocker.presentation.settings.SettingsPresenter
import toothpick.config.Module

class SettingsModule() : Module() {
    init {
        bind(SettingsInteractor::class.java).singletonInScope()
        bind(SettingsPresenter::class.java).singletonInScope()
    }
}