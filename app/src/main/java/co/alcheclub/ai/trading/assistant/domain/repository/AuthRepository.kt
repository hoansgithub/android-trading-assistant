package co.alcheclub.ai.trading.assistant.domain.repository

import android.app.Activity

interface AuthRepository {
    fun isAuthenticated(): Boolean
    fun setAuthenticated(value: Boolean)
    fun logout()
    suspend fun signInWithGoogle(activity: Activity): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
