package com.shakuro.skylocker.di.settings

import com.shakuro.skylocker.presentation.settings.SettingsPresenter
import dagger.Subcomponent

@SettingsScope
@Subcomponent(modules = arrayOf(SettingsModule::class))
interface SettingsComponent {
    fun inject(settingsPresenter: SettingsPresenter)
}
