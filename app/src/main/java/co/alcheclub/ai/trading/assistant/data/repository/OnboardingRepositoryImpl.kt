package co.alcheclub.ai.trading.assistant.data.repository

import android.util.Log
import co.alcheclub.ai.trading.assistant.data.local.PreferencesManager
import co.alcheclub.ai.trading.assistant.domain.repository.OnboardingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class OnboardingRepositoryImpl(
    private val preferencesManager: PreferencesManager,
    private val supabaseClient: SupabaseClient? = null
) : OnboardingRepository {

    companion object {
        private const val TAG = "OnboardingRepo"
    }

    override fun isOnboardingCompleted(): Boolean {
        return preferencesManager.isOnboardingCompleted
    }

    override fun completeOnboarding() {
        preferencesManager.isOnboardingCompleted = true
    }

    /**
     * Mark onboarding complete both locally and in Supabase user_profiles.
     */
    override suspend fun completeOnboarding(userId: UUID) {
        preferencesManager.isOnboardingCompleted = true

        val client = supabaseClient ?: return
        try {
            client.postgrest["user_profiles"]
                .update(
                    kotlinx.serialization.json.buildJsonObject {
                        put("onboarding_completed", kotlinx.serialization.json.JsonPrimitive(true))
                    }
                ) {
                    filter { eq("id", userId.toString()) }
                }
            Log.d(TAG, "Marked onboarding completed in Supabase for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update onboarding status in Supabase (non-fatal)", e)
        }
    }

    override fun isFirstLaunchPending(): Boolean {
        return preferencesManager.isFirstLaunchPending
    }

    override fun clearFirstLaunchPending() {
        preferencesManager.isFirstLaunchPending = false
    }

    /**
     * Sync onboarding status from Supabase.
     * Checks user_profiles.onboarding_completed first,
     * then falls back to checking if user has any strategies.
     * Updates local prefs if server says completed.
     */
    override suspend fun syncOnboardingStatus(userId: UUID) {
        if (preferencesManager.isOnboardingCompleted) return // Already completed locally

        val client = supabaseClient ?: return

        try {
            // Primary: check user_profiles.onboarding_completed
            val profileRows = client.postgrest["user_profiles"]
                .select {
                    filter { eq("id", userId.toString()) }
                    limit(1)
                }
                .decodeList<JsonObject>()

            val profile = profileRows.firstOrNull()
            val onboardingCompleted = profile?.get("onboarding_completed")
                ?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false

            if (onboardingCompleted) {
                Log.d(TAG, "Synced: onboarding completed (from user_profiles)")
                preferencesManager.isOnboardingCompleted = true
                return
            }

            // Fallback: check if user has any strategies
            val strategyRows = client.postgrest["strategies"]
                .select {
                    filter { eq("user_id", userId.toString()) }
                    limit(1)
                }
                .decodeList<JsonObject>()

            if (strategyRows.isNotEmpty()) {
                Log.d(TAG, "Synced: onboarding completed (user has strategies)")
                preferencesManager.isOnboardingCompleted = true
                return
            }

            Log.d(TAG, "Synced: onboarding not yet completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync onboarding status", e)
            // On error, don't block — use local state
        }
    }
}
