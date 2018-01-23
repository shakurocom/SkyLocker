package com.shakuro.skylocker.model.interactor

import com.nhaarman.mockito_kotlin.*
import com.shakuro.skylocker.R
import com.shakuro.skylocker.TestSchedulers
import com.shakuro.skylocker.model.mocks.SettingsRepositoryMock
import com.shakuro.skylocker.model.settings.SettingsInteractor
import com.shakuro.skylocker.model.settings.SettingsRepository
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.model.skyeng.models.db.User
import com.shakuro.skylocker.system.LockServiceManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.terrakok.gitlabclient.model.system.ResourceManager

class SettingsInteractorTest {

    private lateinit var skyEngRepository: SkyEngRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var resourceManager: ResourceManager
    private lateinit var lockServiceManager: LockServiceManager
    private lateinit var settingsInteractor: SettingsInteractor

    private val schedulersProvider = TestSchedulers()

    @Before
    fun setUp() {
        skyEngRepository = mock<SkyEngRepository>()
        resourceManager = mock<ResourceManager>()
        settingsRepository = SettingsRepositoryMock.create()

        lockServiceManager = mock<LockServiceManager> {
            var active = false

            doAnswer { active }.whenever(it).isLockServiceActive()
            doAnswer { active = true }.whenever(it).startLockService()
            doAnswer { active = false }.whenever(it).stopLockService()
        }

        settingsInteractor = SettingsInteractor(skyEngRepository,
                lockServiceManager,
                settingsRepository,
                resourceManager,
                schedulersProvider)
    }

    @Test
    fun check_connected() {
        whenever(skyEngRepository.activeUser()).thenReturn(mock<User>())
        Assert.assertTrue(settingsInteractor.connected)

        whenever(skyEngRepository.activeUser()).thenReturn(null)
        Assert.assertFalse(settingsInteractor.connected)
    }

    @Test
    fun locking_enabled_disabled() {
        val lockEnabledObserver = settingsInteractor.lockChangedObservable.test()
        settingsInteractor.lockingEnabled = true
        verify(lockServiceManager).startLockService()
        Assert.assertTrue(lockServiceManager.isLockServiceActive())
        lockEnabledObserver.assertValue { it }

        val lockDisabledObserver = settingsInteractor.lockChangedObservable.test()
        settingsInteractor.lockingEnabled = false
        verify(lockServiceManager).stopLockService()
        Assert.assertFalse(lockServiceManager.isLockServiceActive())
        lockDisabledObserver.assertValue { !it }
    }

    @Test
    fun use_top1000_words() {
        Assert.assertFalse(lockServiceManager.isLockServiceActive())
        val noQuizesObserver = settingsInteractor.noQuizesObservable.test()

        // quiz exists check
        whenever(skyEngRepository.meaningsExist(any(), any())).thenReturn(true)
        settingsInteractor.useTop1000Words = true
        verify(lockServiceManager).startLockService()
        noQuizesObserver.assertNoValues()

        // no quizes check
        whenever(skyEngRepository.meaningsExist(any(), any())).thenReturn(false)
        settingsInteractor.useTop1000Words = true
        verify(lockServiceManager).stopLockService()
        noQuizesObserver.assertValueCount(1)
    }

    @Test
    fun use_user_words() {
        Assert.assertFalse(lockServiceManager.isLockServiceActive())
        val noQuizesObserver = settingsInteractor.noQuizesObservable.test()

        // quiz exists check
        whenever(skyEngRepository.meaningsExist(any(), any())).thenReturn(true)
        settingsInteractor.useUserWords = true
        verify(lockServiceManager).startLockService()
        noQuizesObserver.assertNoValues()

        // no quizes check
        whenever(skyEngRepository.meaningsExist(any(), any())).thenReturn(false)
        settingsInteractor.useUserWords = true
        verify(lockServiceManager).stopLockService()
        noQuizesObserver.assertValueCount(1)
    }

    @Test
    fun connect_user() {
        settingsInteractor.connectUser("email@google.com", "token").test().apply {
            argumentCaptor<(User?, Throwable?) -> Unit>().apply {
                verify(skyEngRepository).refreshUserMeanings(any(), any(), capture())
                firstValue.invoke(mock<User>(), null)
            }
            awaitTerminalEvent()
            assertComplete()
        }
    }

    @Test
    fun connect_user_error() {
        settingsInteractor.connectUser("email@google.com", "token").test().apply {
            argumentCaptor<(User?, Throwable?) -> Unit>().apply {
                verify(skyEngRepository).refreshUserMeanings(any(), any(), capture())
                firstValue.invoke(null, Error())
            }
            awaitTerminalEvent()
            assertFailure(Error::class.java)
        }
    }

    @Test
    fun connect_user_invalid_email() {
        val token = "token"
        val EMAIL_ERROR = "email_error"
        whenever(resourceManager.getString(R.string.invalid_e_mail)).thenReturn(EMAIL_ERROR)

        settingsInteractor.connectUser(null, token).test().apply {
            awaitTerminalEvent()
            assertError { it.message == EMAIL_ERROR }
        }
        settingsInteractor.connectUser("email", token).test().apply {
            awaitTerminalEvent()
            assertError { it.message == EMAIL_ERROR }
        }
    }

    @Test
    fun connect_user_invalid_token() {
        val email = "email@google.com"
        val TOKEN_ERROR = "token_error"
        whenever(resourceManager.getString(R.string.token_is_empty)).thenReturn(TOKEN_ERROR)

        settingsInteractor.connectUser(email, null).test().apply {
            awaitTerminalEvent()
            assertError { it.message == TOKEN_ERROR }
        }
        settingsInteractor.connectUser(email, " ").test().apply {
            awaitTerminalEvent()
            assertError { it.message == TOKEN_ERROR }
        }
    }

    @Test
    fun disconnect_user() {
        settingsInteractor.disconnectUser().test().awaitTerminalEvent()
        verify(settingsRepository).locksCount = 0L
        verify(skyEngRepository).disconnectActiveUser()
    }

    @Test
    fun request_token() {
        settingsInteractor.requestToken("email@google.com").test().apply {
            argumentCaptor<(Throwable?) -> Unit>().apply {
                verify(skyEngRepository).requestToken(any(), capture())
                firstValue.invoke(null)
            }
            awaitTerminalEvent()
            assertComplete()
        }
    }

    @Test
    fun request_token_error() {
        settingsInteractor.requestToken("email@google.com").test().apply {
            argumentCaptor<(Throwable?) -> Unit>().apply {
                verify(skyEngRepository).requestToken(any(), capture())
                firstValue.invoke(Error())
            }
            awaitTerminalEvent()
            assertError(Error::class.java)
        }
    }

    @Test
    fun request_token_invalid_email() {
        val EMAIL_ERROR = "email_error"
        whenever(resourceManager.getString(R.string.invalid_e_mail)).thenReturn(EMAIL_ERROR)

        settingsInteractor.requestToken(null).test().apply {
            awaitTerminalEvent()
            assertError { it.message == EMAIL_ERROR }
        }
        settingsInteractor.requestToken("email").test().apply {
            awaitTerminalEvent()
            assertError { it.message == EMAIL_ERROR }
        }
    }
}