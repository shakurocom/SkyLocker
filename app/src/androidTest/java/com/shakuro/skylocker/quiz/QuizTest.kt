package com.shakuro.skylocker.quiz

import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.nhaarman.mockito_kotlin.*
import com.shakuro.skylocker.R
import com.shakuro.skylocker.di.Scopes
import com.shakuro.skylocker.entities.Answer
import com.shakuro.skylocker.entities.Quiz
import com.shakuro.skylocker.model.quiz.QuizInteractor
import com.shakuro.skylocker.presentation.quiz.QuizActivity
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith
import toothpick.Toothpick
import toothpick.config.Module


@RunWith(AndroidJUnit4::class)
class QuizTest {

    lateinit var testQuiz: Quiz
    val mockRingObservable: PublishSubject<Unit> = PublishSubject.create<Unit>()

    @Before
    fun before() {
        val answers = (0..QuizInteractor.ALTERNATIVES_COUNT_TO_SHOW)
                .map { Answer("answer $it", it == 0) }
                .toList()
        testQuiz = Quiz("text", "definition", answers)

        Toothpick.openScopes(Scopes.APP_SCOPE, Scopes.SKY_ENG_SCOPE, Scopes.QUIZ_SCOPE)
                .installTestModules(object : Module() {
                    init {
                        bind(QuizInteractor::class.java).toInstance(mock<QuizInteractor>() {
                            // mocking getQuiz()
                            on { getQuiz() } doReturn Single.just(testQuiz)

                            // mocking registerSkipQuizListener()
                            val callbackCaptor = argumentCaptor<(Unit) -> Unit>()
                            doAnswer { mockRingObservable.subscribe(callbackCaptor.firstValue) }
                                    .whenever(it).registerSkipQuizListener(callbackCaptor.capture())
                        })
                    }
                })
    }

    @After
    fun after() {
        Toothpick.closeScope(Scopes.QUIZ_SCOPE)
    }

    @Rule
    @JvmField
    var activityRule = ActivityTestRule<QuizActivity>(QuizActivity::class.java, false, false)

    @Test
    fun test_all_displayed() {
        activityRule.launchActivity(Intent())
        onView(Matchers.allOf(withId(R.id.wordTextView), withText(testQuiz.text)))
                .check(matches(isDisplayed()))
        onView(Matchers.allOf(withId(R.id.definitionTextView), withText(testQuiz.definition)))
                .check(matches(isDisplayed()))
        testQuiz.answers.forEach {
            onView(withText(it.text)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun test_skip() {
        activityRule.launchActivity(Intent())
        onView(withId(R.id.skipQuizButton)).perform(click())
        Assert.assertTrue(activityRule.activity.isFinishing)
    }

    @Test
    fun test_skip_on_ring() {
        activityRule.launchActivity(Intent())
        // make ring
        mockRingObservable.onNext(Unit)
        // assert quiz skipped
        Assert.assertTrue(activityRule.activity.isFinishing)
    }

    @Test
    fun test_right_answer() {
        activityRule.launchActivity(Intent())
        val correctAnswer = testQuiz.answers.filter { it.right }.first()
        onView(withText(correctAnswer.text)).perform(click())
        onView(withText(correctAnswer.text)).check(matches(withGradientDrawable(R.drawable.correct_answer_bg)))
    }

    @Test
    fun test_wrong_answer() {
        activityRule.launchActivity(Intent())
        val correctAnswer = testQuiz.answers.filter { !it.right }.last()
        onView(withText(correctAnswer.text)).perform(click())
        onView(withText(correctAnswer.text)).check(matches(withGradientDrawable(R.drawable.wrond_answer_bg)))
    }

    private fun withGradientDrawable(resourceId: Int): Matcher<View> {
        return object : BoundedMatcher<View, TextView>(TextView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("resource: $resourceId")
            }

            public override fun matchesSafely(textView: TextView): Boolean {
                return textView.background.constantState ==
                        ContextCompat.getDrawable(activityRule.activity, resourceId).constantState
            }
        }
    }
}