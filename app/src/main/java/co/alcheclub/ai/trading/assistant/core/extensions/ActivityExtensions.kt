package co.alcheclub.ai.trading.assistant.core.extensions

import android.app.Activity
import android.os.Build

/**
 * Apply default fade transition for Activity navigation.
 * Uses modern overrideActivityTransition on API 34+, falls back to deprecated
 * overridePendingTransition for backward compatibility.
 */
fun Activity.applyFadeTransition() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_OPEN,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
