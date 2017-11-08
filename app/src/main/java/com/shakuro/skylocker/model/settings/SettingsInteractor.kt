package com.shakuro.skylocker.model.settings

import android.util.Patterns
import com.shakuro.skylocker.R
import com.shakuro.skylocker.model.skyeng.SkyEngRepository
import com.shakuro.skylocker.system.LockServiceManager
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import ru.terrakok.gitlabclient.model.system.ResourceManager
import javax.inject.Inject

class SettingsInteractor @Inject constructor(val skyEngRepository: SkyEngRepository,
                                                 val lockServiceManager: LockServiceManager,
                                                 val settingsRepository: SettingsRepository,
                                                 val resourceManager: ResourceManager) {

    val noQuizesObservable: PublishSubject<Unit> = PublishSubject.create<Unit>()

    val lockChangedObservable
        get() = lockServiceManager.lockServiceObservable

    val connected: Boolean
        get() = skyEngRepository.activeUser() != null

    val email: String
        get() = skyEngRepository.activeUser()?.email ?: ""

    var lockingEnabled: Boolean
        get() {
            val enabled = settingsRepository.lockingEnabled
            if (enabled && !lockServiceManager.isLockServiceActive()) {
                lockServiceManager.startLockService()
            }
            return enabled
        }

        set(value) {
            settingsRepository.lockingEnabled = value
            if (value) {
                lockServiceManager.startLockService()
            } else {
                lockServiceManager.stopLockService()
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

    fun connectUser(email: String?, token: String?):Completable {
        return Completable.create { emitter ->
            if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
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
        }.subscribeOn(Schedulers.io())
    }

    fun disconnectUser(): Completable {
        return Completable.fromRunnable {
            settingsRepository.locksCount = 0
            skyEngRepository.disconnectActiveUser()
        }.subscribeOn(Schedulers.io())
    }

    fun requestToken(email: String?): Completable {
        return Completable.create { emitter ->
            if (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
        }.subscribeOn(Schedulers.io())
    }

    private fun checkLockServiceShouldStartOrStop() {
        val noQuizes = skyEngRepository.randomMeaning() == null
        if (noQuizes) {
            noQuizesObservable.onNext(Unit)
            lockServiceManager.stopLockService()
        } else {
            if ((useTop1000Words || useUserWords) && !lockServiceManager.isLockServiceActive()) {
                lockServiceManager.startLockService()
            }
        }
    }
}