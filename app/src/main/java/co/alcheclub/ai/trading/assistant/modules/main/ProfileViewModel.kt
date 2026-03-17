package co.alcheclub.ai.trading.assistant.modules.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.alcheclub.ai.trading.assistant.BuildConfig
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class UserProfile(
    val email: String = "",
    val displayName: String? = null,
    val memberSince: String = ""
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val supabaseClient: SupabaseClient,
    private val httpClient: HttpClient
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileVM"
    }

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _showSignOutDialog = MutableStateFlow(false)
    val showSignOutDialog: StateFlow<Boolean> = _showSignOutDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onViewAppear() {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val session = supabaseClient.auth.currentSessionOrNull()
        val user = session?.user
        val email = user?.email ?: ""
        val displayName = user?.userMetadata?.get("full_name")?.toString()?.trim('"')
            ?: user?.userMetadata?.get("name")?.toString()?.trim('"')

        val createdAt = user?.createdAt
        val memberSince = if (createdAt != null) {
            try {
                val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                formatter.format(java.util.Date(createdAt.epochSeconds * 1000))
            } catch (_: Exception) { "" }
        } else ""

        _userProfile.value = UserProfile(
            email = email,
            displayName = displayName,
            memberSince = memberSince
        )
    }

    fun requestSignOut() { _showSignOutDialog.value = true }
    fun dismissSignOut() { _showSignOutDialog.value = false }

    fun requestDeleteAccount() { _showDeleteDialog.value = true }
    fun dismissDeleteAccount() { _showDeleteDialog.value = false }

    fun dismissMessage() { _message.value = null }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            _showSignOutDialog.value = false
            _isProcessing.value = true
            authRepository.signOut()
            _isProcessing.value = false
            onComplete()
        }
    }

    /**
     * Delete account via Supabase Edge Function (matching iOS SupabaseAuthService.deleteAccount).
     * The edge function handles: R2 images, DB cascade, auth user removal.
     */
    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            _showDeleteDialog.value = false
            _isDeleting.value = true
            try {
                val session = supabaseClient.auth.currentSessionOrNull()
                    ?: throw Exception("No active session")

                val url = "${BuildConfig.SUPABASE_URL}/functions/v1/delete-user-account"

                val response = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header("x-user-token", "Bearer ${session.accessToken}")
                }

                val statusCode = response.status.value
                if (statusCode !in 200..299) {
                    val body = response.bodyAsText()
                    Log.e(TAG, "Delete account HTTP $statusCode: $body")
                    throw Exception("Account deletion failed: HTTP $statusCode")
                }

                supabaseClient.auth.signOut()
                authRepository.logout()
                _isDeleting.value = false
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Delete account failed", e)
                _isDeleting.value = false
                _message.value = "Failed to delete account. Please try again."
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isProcessing.value = true
            // TODO: Integrate RevenueCat restore
            _isProcessing.value = false
            _message.value = "No active purchases found."
        }
    }
}
