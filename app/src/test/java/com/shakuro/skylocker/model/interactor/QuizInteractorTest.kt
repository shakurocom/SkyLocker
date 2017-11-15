package com.shakuro.skylocker.model.interactor

import com.nhaarman.mockito_kotlin.*
import com.shakuro.skylocker.model.mocks.SettingsRepositoryMock
import com.shakuro.skylocker.model.quiz.QuizInteractor
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.models.db.Alternative
import com.shakuro.skylocker.model.skyeng.models.db.Meaning
import com.shakuro.skylocker.model.skyeng.models.db.User
import org.junit.Before
import org.junit.Test

class QuizInteractorTest {

    private lateinit var skyEngRepository: SkyEngRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var testMeaning: Meaning

    @Before
    fun setUp() {
        skyEngRepository = mock<SkyEngRepository>()
        settingsRepository = SettingsRepositoryMock.create()

        testMeaning = mock<Meaning> {
            on { translation } doReturn "translation"
            on { definition } doReturn "definition"
            on { text } doReturn "text"

            // define alternatives count more than required
            val testAlternativesCount = QuizInteractor.ALTERNATIVES_COUNT_TO_SHOW + 5
            on { alternatives } doReturn (1L..testAlternativesCount)
                    .toList()
                    .map { Alternative(it, "text ${it}", "translation ${it}", it) }
        }
    }

    @Test
    fun get_quiz() {
        whenever(skyEngRepository.randomMeaning(any(), any())).thenReturn(testMeaning)
        whenever(skyEngRepository.activeUser()).thenReturn(mock<User>())

        val quizInteractor = QuizInteractor(skyEngRepository, settingsRepository)
        val testObserver = quizInteractor.getQuiz().test()
        testObserver.awaitTerminalEvent()

        verify(skyEngRepository).randomMeaning(settingsRepository.useTop1000Words,
                settingsRepository.useUserWords)

        testObserver.apply {
            assertNoErrors()
            assertValueCount(1)
            assertValue { it.text == testMeaning.translation.capitalize() }
            assertValue { it.definition == testMeaning.definition.capitalize() }
            assertValue {
                // there should be only one right answer
                it.answers.filter { it.right == true }.size == 1
            }
            assertValue {
                // count of answers should be ALTERNATIVES_COUNT_TO_SHOW + one right answer
                it.answers.size == QuizInteractor.ALTERNATIVES_COUNT_TO_SHOW + 1
            }
        }
    }

    @Test
    fun get_quiz_error() {
        whenever(skyEngRepository.randomMeaning(any(), any())).thenReturn(null)

        val quizInteractor = QuizInteractor(skyEngRepository, settingsRepository)
        val testObserver = quizInteractor.getQuiz().test()
        testObserver.awaitTerminalEvent()

        verify(skyEngRepository)
                .randomMeaning(settingsRepository.useTop1000Words, settingsRepository.useUserWords)
        testObserver
                .assertError { it.message == QuizInteractor.NO_QUIZES_ERROR }
                .assertNoValues()
    }

    @Test
    fun check_refresh_called() {
        whenever(skyEngRepository.randomMeaning(any(), any())).thenReturn(testMeaning)
        whenever(skyEngRepository.activeUser()).thenReturn(mock<User>())

        val quizInteractor = QuizInteractor(skyEngRepository, settingsRepository)
        (1..QuizInteractor.QUIZES_COUNT_TO_REFRESH).forEach {
            verify(skyEngRepository, never()).requestUserMeaningsUpdate()
            quizInteractor.getQuiz().test().apply {
                awaitTerminalEvent()
                assertNoErrors()
            }
        }
        verify(skyEngRepository).requestUserMeaningsUpdate()
    }

    @Test
    fun check_refresh_not_called() {
        whenever(skyEngRepository.randomMeaning(any(), any())).thenReturn(testMeaning)
        whenever(skyEngRepository.activeUser()).thenReturn(null)

        val quizInteractor = QuizInteractor(skyEngRepository, settingsRepository)
        (1..QuizInteractor.QUIZES_COUNT_TO_REFRESH).forEach {
            quizInteractor.getQuiz().test().apply {
                awaitTerminalEvent()
                assertNoErrors()
            }
        }
        verify(skyEngRepository, never()).requestUserMeaningsUpdate()
    }
}