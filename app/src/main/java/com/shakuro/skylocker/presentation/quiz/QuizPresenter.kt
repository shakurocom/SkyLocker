package com.shakuro.skylocker.presentation.quiz

import android.content.Intent
import android.os.Handler
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.VIEW_ALTERNATIVES_COUNT
import com.shakuro.skylocker.model.entities.Meaning
import com.shakuro.skylocker.system.RingStateManager
import com.shakuro.skylocker.ui.BlurredImageLoader
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject

@InjectViewState
class QuizPresenter : MvpPresenter<QuizView>() {

    @Inject
    lateinit var skyLockerManager: SkyLockerManager

    @Inject
    lateinit var blurredImageLoader: BlurredImageLoader

    @Inject
    lateinit var ringStateManager: RingStateManager
    var ringStateSubscription: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val backgroundImage = blurredImageLoader.getBlurredBgImage()
        backgroundImage?.let { viewState.setBackgroundImage(it) }

        // skip quiz on phone ringing
        ringStateSubscription = ringStateManager.register().subscribe { skipQuiz() }

        showQuiz()
    }

    override fun onDestroy() {
        super.onDestroy()
        ringStateSubscription?.dispose()
    }

    fun checkAnswer(answer: Answer) {
        viewState.onAnswerChecked(answer, answer.right)
        viewState.disableControls()

        val delay = if (answer.right) 500L else 1000L
        Handler().postDelayed({
            viewState.unlockDevice()
        }, delay)
    }

    fun onSkipAction() {
        skipQuiz()
    }

    fun checkApplicationWasKilledBySystem(intent: Intent?) {
        intent?.let {
            if (it.hasExtra("kill") && it.extras.getInt("kill") == 1) {
                skipQuiz()
            }
        }
    }

    private fun showQuiz() {
        val meaning = skyLockerManager.randomMeaning()

        if (meaning != null) {
            viewState.setQuizTranslation(meaning.translation.capitalize())
            viewState.setQuizDefinition(meaning.definition.capitalize())

            viewState.clearAnswers()
            quizAnswers(meaning).forEach {
                viewState.addAnswer(it)
            }
        } else {
            skipQuiz()
        }
    }

    private fun skipQuiz() {
        viewState.unlockDevice()
    }

    private fun quizAnswers(meaning: Meaning): List<Answer> {
        val answers = mutableListOf<Answer>()
        answers.add(Answer(meaning.text.capitalize(), true))

        val alternatives = meaning.alternatives
        val alternativesCount = Math.min(VIEW_ALTERNATIVES_COUNT, alternatives.size)
        (0..alternativesCount - 1).mapTo(answers) {
            val alternative = alternatives[it]
            Answer(alternative.text.capitalize(), false)
        }

        Collections.shuffle(answers)
        return answers
    }
}