package com.shakuro.skylocker.di.application

import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.di.quiz.QuizComponent
import com.shakuro.skylocker.di.quiz.QuizModule
import com.shakuro.skylocker.di.settings.SettingsComponent
import com.shakuro.skylocker.di.settings.SettingsModule
import com.shakuro.skylocker.presentation.settings.SettingsPresenter
import com.shakuro.skylocker.system.services.BootCompletedIntentReceiver
import com.shakuro.skylocker.system.services.LockscreenIntentReceiver
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(ApplicationModule::class, SkyEngModule::class))
@Singleton
interface AppComponent {

    fun plusQuizComponent(quizModule: QuizModule): QuizComponent
    fun plusSettingsModule(settingsModule: SettingsModule): SettingsComponent

    fun inject(application: SkyLockerApp)
    fun inject(bootCompletedIntentReceiver: BootCompletedIntentReceiver)
    fun inject(lockscreenIntentReceiver: LockscreenIntentReceiver)
}