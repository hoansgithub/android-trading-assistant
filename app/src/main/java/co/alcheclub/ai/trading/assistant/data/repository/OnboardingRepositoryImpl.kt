package co.alcheclub.ai.trading.assistant.data.repository

import co.alcheclub.ai.trading.assistant.data.local.PreferencesManager
import co.alcheclub.ai.trading.assistant.domain.repository.OnboardingRepository

class OnboardingRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : OnboardingRepository {

    override fun isOnboardingCompleted(): Boolean {
        return preferencesManager.isOnboardingCompleted
    }

    override fun completeOnboarding() {
        preferencesManager.isOnboardingCompleted = true
    }

    override fun isFirstLaunchPending(): Boolean {
        return preferencesManager.isFirstLaunchPending
    }

    override fun clearFirstLaunchPending() {
        preferencesManager.isFirstLaunchPending = false
    }
}
