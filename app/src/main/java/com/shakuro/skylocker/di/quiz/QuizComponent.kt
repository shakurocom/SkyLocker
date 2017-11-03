package com.shakuro.skylocker.di.quiz

import com.shakuro.skylocker.presentation.quiz.QuizPresenter
import dagger.Subcomponent

@QuizScope
@Subcomponent(modules = arrayOf(QuizModule::class))
interface QuizComponent {
    fun inject(quizPresenter: QuizPresenter)
}
