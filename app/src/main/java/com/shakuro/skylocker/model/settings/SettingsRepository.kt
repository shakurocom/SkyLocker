package com.shakuro.skylocker.model.settings

import android.content.SharedPreferences
import javax.inject.Inject

class SettingsRepository @Inject constructor(private val preferences: SharedPreferences) {

    private val LOCKING_ENABLED = "LOCKING_ENABLED"
    private val USE_TOP_1000_WORDS_KEY = "USE_TOP_1000_WORDS_KEY"
    private val USE_USER_WORDS_KEY = "USE_USER_WORDS_KEY"
    private val LOCKS_COUNT_KEY = "LOCKS_COUNT_KEY"

    var lockingEnabled: Boolean
        get() = preferences.getBoolean(LOCKING_ENABLED, true)
        set(value) = preferences.edit().putBoolean(LOCKING_ENABLED, value).apply()

    var useTop1000Words: Boolean
        get() = preferences.getBoolean(USE_TOP_1000_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_TOP_1000_WORDS_KEY, value).apply()

    var useUserWords: Boolean
        get() = preferences.getBoolean(USE_USER_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_USER_WORDS_KEY, value).apply()

    var locksCount: Long
        get() = preferences.getLong(LOCKS_COUNT_KEY, 0)
        set(value) = preferences.edit().putLong(LOCKS_COUNT_KEY, value).apply()
}
