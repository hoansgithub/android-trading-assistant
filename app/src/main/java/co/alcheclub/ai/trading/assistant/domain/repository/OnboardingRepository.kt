package co.alcheclub.ai.trading.assistant.domain.repository

interface OnboardingRepository {
    fun isOnboardingCompleted(): Boolean
    fun completeOnboarding()
    suspend fun completeOnboarding(userId: java.util.UUID)
    fun isFirstLaunchPending(): Boolean
    fun clearFirstLaunchPending()
    suspend fun syncOnboardingStatus(userId: java.util.UUID)
}
