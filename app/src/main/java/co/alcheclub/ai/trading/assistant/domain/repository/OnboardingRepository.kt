package co.alcheclub.ai.trading.assistant.domain.repository

interface OnboardingRepository {
    fun isOnboardingCompleted(): Boolean
    fun completeOnboarding()
    fun isFirstLaunchPending(): Boolean
    fun clearFirstLaunchPending()
}
