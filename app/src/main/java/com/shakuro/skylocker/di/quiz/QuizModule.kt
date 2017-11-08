package com.shakuro.skylocker.di.quiz

import com.shakuro.skylocker.model.quiz.QuizBgImageLoader
import com.shakuro.skylocker.model.quiz.QuizInteractor
import com.shakuro.skylocker.presentation.quiz.QuizPresenter
import toothpick.config.Module

class QuizModule() : Module() {
    init {
        bind(QuizInteractor::class.java).singletonInScope()
        bind(QuizBgImageLoader::class.java).singletonInScope()
        bind(QuizPresenter::class.java).singletonInScope()
    }
}