package com.shakuro.skylocker.model.quiz

import com.shakuro.skylocker.entities.Answer
import com.shakuro.skylocker.entities.Quiz
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.models.db.Meaning
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

open class QuizInteractor @Inject constructor(private val skyEngRepository: SkyEngRepository,
                                         private val settingsRepository: SettingsRepository) {

    companion object {
        val ALTERNATIVES_COUNT_TO_SHOW = 3
        val QUIZES_COUNT_TO_REFRESH = 5
        val NO_QUIZES_ERROR = "There is no any quiz"
    }

    open fun getQuiz(): Single<Quiz> {
        return Single.fromCallable<Quiz> {
            checkRefreshRequired()

            val useTop1000Words = settingsRepository.useTop1000Words
            val useUserWords = settingsRepository.useUserWords

            val meaning = skyEngRepository.randomMeaning(useTop1000Words, useUserWords)
            if (meaning != null) {
                Quiz(meaning.translation.capitalize(), meaning.definition.capitalize(), quizAnswers(meaning))
            } else {
                throw Error(NO_QUIZES_ERROR)
            }
        }
    }

    private fun quizAnswers(meaning: Meaning): List<Answer> {
        val answers = mutableListOf<Answer>()
        answers.add(Answer(meaning.text.capitalize(), true))

        val alternatives = meaning.alternatives
        val alternativesCount = Math.min(ALTERNATIVES_COUNT_TO_SHOW, alternatives.size)
        (0..alternativesCount - 1).mapTo(answers) {
            val alternative = alternatives[it]
            Answer(alternative.text.capitalize(), false)
        }

        Collections.shuffle(answers)
        return answers
    }

    private fun checkRefreshRequired() {
        if (skyEngRepository.activeUser() != null) {
            settingsRepository.locksCount++
            if (settingsRepository.locksCount % QUIZES_COUNT_TO_REFRESH == 0L) {
                skyEngRepository.requestUserMeaningsUpdate()
            }
        }
    }
}