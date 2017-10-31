package com.shakuro.skylocker.di

import com.shakuro.skylocker.SkyLockerApp
import com.shakuro.skylocker.di.modules.BlurredImageModule
import com.shakuro.skylocker.di.modules.SkyLockerManagerModule
import com.shakuro.skylocker.lock.BootCompletedIntentReceiver
import com.shakuro.skylocker.lock.LockscreenIntentReceiver
import com.shakuro.skylocker.presentation.quiz.QuizActivity
import com.shakuro.skylocker.presentation.quiz.QuizPresenter
import com.shakuro.skylocker.presentation.settings.SettingsPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(SkyLockerManagerModule::class, BlurredImageModule::class))
interface AppComponent {
    fun inject(application: SkyLockerApp)
    fun inject(settingsPresenter: SettingsPresenter)
    fun inject(quizActivity: QuizActivity)
    fun inject(quizPresenter: QuizPresenter)
    fun inject(bootCompletedIntentReceiver: BootCompletedIntentReceiver)
    fun inject(lockscreenIntentReceiver: LockscreenIntentReceiver)
}