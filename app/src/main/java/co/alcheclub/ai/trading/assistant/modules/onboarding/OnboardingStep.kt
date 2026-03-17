package co.alcheclub.ai.trading.assistant.modules.onboarding

enum class OnboardingStep(val stepNumber: Int) {
    EXPERIENCE_LEVEL(1),
    TIME_AVAILABILITY(2),
    RISK_COMFORT(3),
    PRIMARY_GOAL(4),
    LEARNING_STYLE(5),
    RATE_US(6),
    PROCESSING(7),
    DISCLAIMER(8),
    ALL_SET(9);

    val showsHeader: Boolean get() = stepNumber <= 6

    companion object {
        const val SURVEY_STEP_COUNT = 6
    }
}
