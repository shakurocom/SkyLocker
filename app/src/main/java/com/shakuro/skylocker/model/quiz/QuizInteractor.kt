package com.shakuro.skylocker.model.quiz

import com.shakuro.skylocker.entities.Answer
import com.shakuro.skylocker.entities.Quiz
import com.shakuro.skylocker.model.SkyLockerManager
import com.shakuro.skylocker.model.VIEW_ALTERNATIVES_COUNT
import com.shakuro.skylocker.model.models.db.Meaning
import io.reactivex.Single
import java.util.*

class QuizInteractor(private val skyLockerManager: SkyLockerManager) {

    fun getQuiz() = Single.fromCallable<Quiz> {
        val meaning = skyLockerManager.randomMeaning()
        if (meaning != null) {
            Quiz(meaning.translation.capitalize(), meaning.definition.capitalize(), quizAnswers(meaning))
        } else {
            throw Error("There is no any quiz")
        }
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