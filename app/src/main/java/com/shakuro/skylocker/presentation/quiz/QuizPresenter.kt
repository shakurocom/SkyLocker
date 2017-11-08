package com.shakuro.skylocker.presentation.quiz

import android.content.Intent
import android.view.View
import com.arellomobile.mvp.InjectViewState
import com.shakuro.skylocker.entities.Answer
import com.shakuro.skylocker.entities.Quiz
import com.shakuro.skylocker.extension.addTo
import com.shakuro.skylocker.model.quiz.QuizBgImageLoader
import com.shakuro.skylocker.model.quiz.QuizInteractor
import com.shakuro.skylocker.presentation.common.BasePresenter
import com.shakuro.skylocker.system.RingStateManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class QuizPresenter @Inject constructor(val quizInteractor: QuizInteractor,
                                        val quizBgImageLoader: QuizBgImageLoader,
                                        val ringStateManager: RingStateManager) : BasePresenter<QuizView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        // skip quiz on phone ringing
        ringStateManager.register()
                .subscribe({ skipQuiz() },
                        { error -> println("ringState error: ${error.localizedMessage}") })
                .addTo(disposeOnDestroy)

        // show quiz
        quizInteractor.getQuiz()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ quiz -> showQuiz(quiz) },
                        {
                            error -> skipQuiz()
                            println("quiz skipped with error: ${error.localizedMessage}")
                        })
                .addTo(disposeOnDestroy)
    }

    fun onBackgroundImageRequest() {
        quizBgImageLoader.loadBgImage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ image -> viewState.setBackgroundImage(image) },
                        { error -> println("imageLoader error: ${error.localizedMessage}") })
                .addTo(disposeOnDestroy)
    }

    fun checkAnswer(answer: Answer, answerView: View) {
        viewState.disableControls()
        viewState.updateSelectedAnswer(answer, answerView)

        val delay = if (answer.right) 500L else 1000L
        Observable.timer(delay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { viewState.unlockDevice() }
                .addTo(disposeOnDestroy)
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

    private fun showQuiz(quiz: Quiz) {
        viewState.clearAnswers()
        viewState.setQuizTranslation(quiz.text)
        viewState.setQuizDefinition(quiz.definition)
        quiz.answers.forEach {
            viewState.addAnswer(it)
        }
    }

    private fun skipQuiz() {
        viewState.unlockDevice()
    }
}
