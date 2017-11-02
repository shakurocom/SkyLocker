package com.shakuro.skylocker.model.settings

import android.content.SharedPreferences

class SettingsRepository(val preferences: SharedPreferences) {

    private val LOCKING_ENABLED = "LOCKING_ENABLED"
    private val USE_TOP_1000_WORDS_KEY = "USE_TOP_1000_WORDS_KEY"
    private val USE_USER_WORDS_KEY = "USE_USER_WORDS_KEY"

    var lockingEnabled: Boolean
        get() = preferences.getBoolean(LOCKING_ENABLED, true)
        set(value) = preferences.edit().putBoolean(LOCKING_ENABLED, value).apply()

    var useTop1000Words: Boolean
        get() = preferences.getBoolean(USE_TOP_1000_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_TOP_1000_WORDS_KEY, value).apply()

    var useUserWords: Boolean
        get() = preferences.getBoolean(USE_USER_WORDS_KEY, true)
        set(value) = preferences.edit().putBoolean(USE_USER_WORDS_KEY, value).apply()
}
