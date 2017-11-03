package com.shakuro.skylocker.model.quiz

import com.shakuro.skylocker.entities.Answer
import com.shakuro.skylocker.entities.Quiz
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.models.db.Meaning
import io.reactivex.Single
import java.util.*

class QuizInteractor(private val skyEngRepository: SkyEngRepository, private val settingsRepository: SettingsRepository) {

    private val ALTERNATIVES_COUNT_TO_SHOW = 3
    private val QUIZES_COUNT_TO_REFRESH = 5

    fun getQuiz(): Single<Quiz> {
        return Single.fromCallable<Quiz> {
            checkRefreshRequired()

            val meaning = skyEngRepository.randomMeaning()
            if (meaning != null) {
                Quiz(meaning.translation.capitalize(), meaning.definition.capitalize(), quizAnswers(meaning))
            } else {
                throw Error("There is no any quiz")
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