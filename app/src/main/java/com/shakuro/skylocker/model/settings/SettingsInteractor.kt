package com.shakuro.skylocker.model.settings

import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.system.LockServiceManager
import com.shakuro.skylocker.system.SchedulersProvider
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import ru.terrakok.gitlabclient.model.system.ResourceManager
import java.util.regex.Pattern
import javax.inject.Inject

class SettingsInteractor @Inject constructor(val skyEngRepository: SkyEngRepository,
                                             val lockServiceManager: LockServiceManager,
                                             val settingsRepository: SettingsRepository,
                                             val resourceManager: ResourceManager,
                                             val schedulersProvider: SchedulersProvider) {
    private val EMAIL_ADDRESS = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    )

    val noQuizesObservable = PublishSubject.create<Unit>()
    val lockChangedObservable = PublishSubject.create<Boolean>()

    val connected: Boolean
        get() = skyEngRepository.activeUser() != null

    val email: String
        get() = skyEngRepository.activeUser()?.email ?: ""

    var lockingEnabled: Boolean
        get() {
            val enabled = settingsRepository.lockingEnabled
            if (enabled && !lockServiceManager.isLockServiceActive()) {
                lockServiceManager.startLockService()
                lockChangedObservable.onNext(true)
            }
            return enabled
        }

        set(value) {
            settingsRepository.lockingEnabled = value
            if (value) {
                lockServiceManager.startLockService()
                lockChangedObservable.onNext(true)
            } else {
                lockServiceManager.stopLockService()
                lockChangedObservable.onNext(false)
            }
        }

    var useTop1000Words: Boolean
        get() = settingsRepository.useTop1000Words

        set(value) {
            settingsRepository.useTop1000Words = value
            checkLockServiceShouldStartOrStop()
        }

    var useUserWords: Boolean
        get() = settingsRepository.useUserWords

        set(value) {
            settingsRepository.useUserWords = value
            checkLockServiceShouldStartOrStop()
        }

    fun connectUser(email: String?, token: String?): Completable {
        return Completable.create { emitter ->
            if (email == null || !EMAIL_ADDRESS.matcher(email).matches()) {
                emitter.onError(Error(resourceManager.getString(R.string.invalid_e_mail)))
                return@create
            }
            if (token == null || token.trim().isEmpty()) {
                emitter.onError(Error(resourceManager.getString(R.string.token_is_empty)))
                return@create
            }
            skyEngRepository.refreshUserMeanings(email, token, { _, error ->
                if (error == null) {
                    emitter.onComplete()
                } else {
                    emitter.onError(error)
                }
            })
        }.subscribeOn(schedulersProvider.io())
    }

    fun disconnectUser(): Completable {
        return Completable.fromRunnable {
            settingsRepository.locksCount = 0
            skyEngRepository.disconnectActiveUser()
        }.subscribeOn(schedulersProvider.io())
    }

    fun requestToken(email: String?): Completable {
        return Completable.create { emitter ->
            if (email != null && EMAIL_ADDRESS.matcher(email).matches()) {
                skyEngRepository.requestToken(email, { error ->
                    if (error == null) {
                        emitter.onComplete()
                    } else {
                        emitter.onError(Error(error.localizedMessage))
                    }
                })
            } else {
                emitter.onError(Error(resourceManager.getString(R.string.invalid_e_mail)))
            }
        }.subscribeOn(schedulersProvider.io())
    }

    private fun checkLockServiceShouldStartOrStop() {
        val noQuizes = !skyEngRepository.meaningsExist(useTop1000Words, useUserWords)
        if (noQuizes) {
            noQuizesObservable.onNext(Unit)
            lockingEnabled = false
        } else {
            if ((useTop1000Words || useUserWords) && !lockingEnabled) {
                lockingEnabled = true
            }
        }
    }
}