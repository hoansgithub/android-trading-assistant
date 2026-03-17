package co.alcheclub.ai.trading.assistant.data.repository

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import co.alcheclub.ai.trading.assistant.BuildConfig
import co.alcheclub.ai.trading.assistant.data.local.PreferencesManager
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import java.security.MessageDigest
import java.util.UUID

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

    override fun getCurrentUserId(): UUID {
        val session = supabaseClient.auth.currentSessionOrNull()
        return session?.user?.id?.let {
            try { UUID.fromString(it) } catch (_: Exception) { UUID.randomUUID() }
        } ?: UUID.randomUUID()
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<Unit> {
        return try {
            android.util.Log.d(TAG, "Starting Google sign-in...")

            // Generate nonce for security (Supabase docs requirement)
            val rawNonce = UUID.randomUUID().toString()
            val hashedNonce = hashNonce(rawNonce)

            val idToken = getGoogleIdToken(activity, hashedNonce)
            android.util.Log.d(TAG, "Got Google ID token (${idToken.take(20)}...)")

            // Sign in with Supabase using the Google ID token + raw nonce
            android.util.Log.d(TAG, "Signing in with Supabase...")
            supabaseClient.auth.signInWith(IDToken) {
                provider = Google
                this.idToken = idToken
                this.nonce = rawNonce
            }
            android.util.Log.d(TAG, "Supabase sign-in successful!")

            // Mark as authenticated locally
            preferencesManager.isAuthenticated = true

            Result.success(Unit)
        } catch (e: GetCredentialCancellationException) {
            android.util.Log.d(TAG, "Sign-in cancelled by user")
            Result.failure(Exception("Sign-in was cancelled"))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Google sign-in failed: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * SHA-256 hash the nonce for Google ID option.
     * Google gets the hashed nonce, Supabase gets the raw nonce to verify.
     */
    private fun hashNonce(rawNonce: String): String {
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Try GetGoogleIdOption first (silent/saved accounts).
     * If no credentials available, fall back to GetSignInWithGoogleOption
     * which always shows the Google Sign-In bottom sheet.
     */
    private suspend fun getGoogleIdToken(activity: Activity, hashedNonce: String): String {
        val credentialManager = CredentialManager.create(activity)

        // First try: GetGoogleIdOption (checks saved/authorized accounts)
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            return extractGoogleIdToken(result.credential)
        } catch (e: NoCredentialException) {
            android.util.Log.d(TAG, "No saved credentials, using Sign In With Google")
        }

        // Fallback: GetSignInWithGoogleOption (always shows Google sign-in UI)
        val signInOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        val result = credentialManager.getCredential(activity, request)
        return extractGoogleIdToken(result.credential)
    }

    private fun extractGoogleIdToken(credential: androidx.credentials.Credential): String {
        android.util.Log.d(TAG, "Credential type: ${credential.type}, class: ${credential.javaClass.simpleName}")

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            android.util.Log.d(TAG, "Extracted Google ID token for: ${googleCredential.displayName}")
            return googleCredential.idToken
        }

        // Try to extract from data bundle directly as fallback
        val idToken = credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
        if (idToken != null) {
            android.util.Log.d(TAG, "Extracted ID token from bundle fallback")
            return idToken
        }

        android.util.Log.e(TAG, "Unexpected credential type: ${credential.type}, data keys: ${credential.data.keySet()}")
        throw IllegalStateException("Unexpected credential type: ${credential.type}")
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

    companion object {
        private const val TAG = "AuthRepository"
    }
}
