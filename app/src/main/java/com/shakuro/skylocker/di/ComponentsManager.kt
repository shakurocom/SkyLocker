package com.shakuro.skylocker.di

import android.content.Context
import com.shakuro.skylocker.di.application.AppComponent
import com.shakuro.skylocker.di.application.ApplicationModule
import com.shakuro.skylocker.di.application.DaggerAppComponent
import com.shakuro.skylocker.di.quiz.QuizComponent
import com.shakuro.skylocker.di.quiz.QuizModule
import com.shakuro.skylocker.di.settings.SettingsComponent
import com.shakuro.skylocker.di.settings.SettingsModule

class ComponentsManager(context: Context) {
    val context = context.applicationContext

    private var appComponentField: AppComponent? = null
    val appComponent: AppComponent
        get() {
            if (appComponentField == null) {
                appComponentField = DaggerAppComponent.builder()
                        .applicationModule(ApplicationModule(context))
                        .build()
            }
            return appComponentField ?: throw Exception("${AppComponent::class} not initialized")
        }

    private var quizComponentField: QuizComponent? = null
    val quizComponent: QuizComponent
        get() {
            if (quizComponentField == null) {
                quizComponentField = appComponent.plusQuizComponent(QuizModule())
            }
            return quizComponentField ?: throw Exception("${QuizComponent::class} not initialized")
        }

    fun clearQuizComponent() {
        quizComponentField = null
    }

    private var settingsComponentField: SettingsComponent? = null
    val settingsComponent: SettingsComponent
        get() {
            if (settingsComponentField == null) {
                settingsComponentField = appComponent.plusSettingsModule(SettingsModule())
            }
            return settingsComponentField ?: throw Exception("${SettingsModule::class} not initialized")
        }

    fun clearSettingsComponent() {
        settingsComponentField = null
    }
}
