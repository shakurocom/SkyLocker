package com.shakuro.skylocker.presentation.quiz

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface QuizView : MvpView {

    fun unlockDevice()

    fun clearAnswers()

    fun addAnswer(answer: Answer)

    fun onAnswerChecked(answer: Answer, right: Boolean)

    fun setBackgroundImage(image: Bitmap)

    fun setQuizTranslation(translation: String)

    fun setQuizDefinition(definition: String)

    fun disableControls()
}