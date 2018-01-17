package com.shakuro.skylocker.settings

import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.Checkable
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.shakuro.skylocker.R
import com.shakuro.skylocker.di.Scopes
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.models.db.User
import com.shakuro.skylocker.presentation.settings.SettingsActivity
import com.shakuro.skylocker.system.LockServiceManager
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.*
import org.junit.runner.RunWith
import toothpick.Toothpick
import toothpick.config.Module

/**
 * Created by anatoly on 17.01.18.
 */
@RunWith(AndroidJUnit4::class)
class SettingsTest {
    private lateinit var context: Context
    private lateinit var lockServiceManager: LockServiceManager
    lateinit var testUser: User

    @Before
    fun before() {
        context = InstrumentationRegistry.getContext()
        lockServiceManager = LockServiceManager(context)

        testUser = User(1, "test@test.com", "1234")

        Toothpick.openScopes(Scopes.APP_SCOPE, Scopes.SKY_ENG_SCOPE)
            .installTestModules(object : Module() {
                init {
                    bind(SkyEngRepository::class.java).toInstance(mock<SkyEngRepository>() {
                        on { activeUser() } doReturn testUser
                    })
                }
            })

        activityRule.launchActivity(Intent())
    }

    @After
    fun after() {
        Toothpick.closeScope(Scopes.SKY_ENG_SCOPE)
    }

    @Rule
    @JvmField
    var activityRule = ActivityTestRule<SettingsActivity>(SettingsActivity::class.java, false, false)

    @Test
    fun test_switch_on() {
        onView(withId(R.id.switchForActionBar)).perform(setChecked(true));
        Assert.assertTrue(lockServiceManager.isLockServiceActive())
    }

    @Test
    fun test_switch_off() {
        onView(withId(R.id.switchForActionBar)).perform(setChecked(false))
        Assert.assertFalse(lockServiceManager.isLockServiceActive())
    }

    @Test
    fun test_top1000WordsCheckBox_on() {
        onView(withId(R.id.top1000WordsCheckBox)).perform(setChecked(true))
        onView(withId(R.id.switchForActionBar)).check(matches(isChecked()))
        Assert.assertTrue(lockServiceManager.isLockServiceActive())
    }

    @Test
    fun test_top1000WordsCheckBox_off() {
        onView(withId(R.id.top1000WordsCheckBox)).perform(setChecked(false))
        onView(withId(R.id.switchForActionBar)).check(matches(isNotChecked()))
        Assert.assertFalse(lockServiceManager.isLockServiceActive())
    }

    @Test
    fun test_connect_successful() {
//        activityRule.launchActivity(Intent())
//        whenever(skyEngRepository.activeUser()).thenReturn(mock<User>())
//        onView(withId(R.id.emailEditText)).perform(ViewActions.replaceText("test@test.com"))
//        onView(withId(R.id.tokenEditText)).perform(ViewActions.replaceText("1234"))
//        onView(withId(R.id.tokenEditText)).perform(ViewActions.click())
    }

    fun setChecked(checked: Boolean): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return object : Matcher<View> {
                    override fun matches(item: Any): Boolean {
                        return isA(Checkable::class.java).matches(item)
                    }

                    override fun describeMismatch(item: Any, mismatchDescription: Description) {}

                    override fun _dont_implement_Matcher___instead_extend_BaseMatcher_() {}

                    override fun describeTo(description: Description) {}
                }
            }

            override fun getDescription(): String? {
                return null
            }

            override fun perform(uiController: UiController, view: View) {
                val checkableView = view as Checkable
                checkableView.isChecked = checked
            }
        }
    }
}