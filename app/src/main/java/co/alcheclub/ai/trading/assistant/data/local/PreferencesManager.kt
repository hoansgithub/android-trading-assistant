package co.alcheclub.ai.trading.assistant.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var isAuthenticated: Boolean
        get() = prefs.getBoolean(KEY_AUTHENTICATED, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTHENTICATED, value).apply()

    var isFirstLaunchPending: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH_PENDING, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH_PENDING, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "alpha_profit_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_AUTHENTICATED = "authenticated"
        private const val KEY_FIRST_LAUNCH_PENDING = "first_launch_pending"
    }
}
