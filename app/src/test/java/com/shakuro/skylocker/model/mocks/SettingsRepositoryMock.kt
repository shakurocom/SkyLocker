package com.shakuro.skylocker.model.mocks

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.shakuro.skylocker.model.settings.SettingsRepository
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyLong

class SettingsRepositoryMock {

    companion object {

        fun create(): SettingsRepository {
            return mock<SettingsRepository> {
                var mockUseTop1000Words = true
                doAnswer { mockUseTop1000Words }.whenever(it).useTop1000Words
                doAnswer { mockUseTop1000Words = it.arguments[0] as Boolean }.whenever(it).useTop1000Words = anyBoolean()

                var mockUseUserWords = true
                doAnswer { mockUseUserWords }.whenever(it).useUserWords
                doAnswer { mockUseUserWords = it.arguments[0] as Boolean }.whenever(it).useUserWords = anyBoolean()

                var mockLockingEnabled = true
                doAnswer { mockLockingEnabled }.whenever(it).lockingEnabled
                doAnswer { mockLockingEnabled = it.arguments[0] as Boolean }.whenever(it).lockingEnabled = anyBoolean()

                var mockLocksCount = 0L
                doAnswer { mockLocksCount }.whenever(it).locksCount
                doAnswer { mockLocksCount = it.arguments[0] as Long }.whenever(it).locksCount = anyLong()
            }
        }
    }
}
