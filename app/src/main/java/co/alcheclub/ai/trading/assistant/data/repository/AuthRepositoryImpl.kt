package co.alcheclub.ai.trading.assistant.data.repository

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import co.alcheclub.ai.trading.assistant.BuildConfig
import co.alcheclub.ai.trading.assistant.data.local.PreferencesManager
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken

class AuthRepositoryImpl(
    private val preferencesManager: PreferencesManager,
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override fun isAuthenticated(): Boolean {
        return preferencesManager.isAuthenticated
    }

    override fun setAuthenticated(value: Boolean) {
        preferencesManager.isAuthenticated = value
    }

    override fun logout() {
        preferencesManager.clear()
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<Unit> {
        return try {
            // Step 1: Get Google ID token via Credential Manager
            val credentialManager = CredentialManager.create(activity)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            // Step 2: Sign in with Supabase using the Google ID token
            supabaseClient.auth.signInWith(IDToken) {
                provider = Google
                this.idToken = idToken
            }

            // Step 3: Mark as authenticated locally
            preferencesManager.isAuthenticated = true

            Result.success(Unit)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Sign-in was cancelled"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            preferencesManager.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            // Still clear local state even if remote sign-out fails
            preferencesManager.clear()
            Result.failure(e)
        }
    }
}
