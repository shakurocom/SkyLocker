package com.shakuro.skylocker.presentation.quiz

import android.graphics.Bitmap
import android.view.View
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface QuizView : MvpView {

    fun unlockDevice()

    fun clearAnswers()

    fun addAnswer(answer: Answer)

    fun setBackgroundImage(image: Bitmap)

    fun setQuizTranslation(translation: String)

    fun setQuizDefinition(definition: String)

    fun disableControls()
}