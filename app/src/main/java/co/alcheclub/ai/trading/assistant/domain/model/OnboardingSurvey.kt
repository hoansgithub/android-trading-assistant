package co.alcheclub.ai.trading.assistant.domain.model

enum class ExperienceLevel(
    val emoji: String,
    val displayName: String,
    val subtitle: String
) {
    BEGINNER("\uD83C\uDF31", "I'm new to this", "Just starting my trading journey"),
    SOME("\uD83D\uDCCA", "I know the basics", "Understand charts and basic terms"),
    INTERMEDIATE("\uD83D\uDCC8", "I trade regularly", "Comfortable with technical analysis"),
    ADVANCED("\uD83C\uDFC6", "I'm experienced", "Years of active trading experience")
}

enum class TimeAvailability(
    val emoji: String,
    val displayName: String,
    val subtitle: String
) {
    WEEKLY("\uD83D\uDCC5", "A few times a week", "Weekend warrior approach"),
    DAILY("☀\uFE0F", "Once a day", "Quick daily check-in"),
    FEW_PER_DAY("⏰", "A few times a day", "Regular monitoring throughout the day"),
    HOURLY("⚡", "I'm always watching", "Full-time market monitoring")
}

enum class RiskComfort(
    val emoji: String,
    val displayName: String,
    val subtitle: String
) {
    CONSERVATIVE("\uD83D\uDEE1\uFE0F", "Play it safe", "Prefer smaller, consistent gains"),
    MODERATE("⚖\uFE0F", "Balanced approach", "Willing to take calculated risks"),
    AGGRESSIVE("\uD83D\uDD25", "High risk, high reward", "Comfortable with large swings")
}

enum class PrimaryGoal(
    val emoji: String,
    val displayName: String,
    val subtitle: String
) {
    LEARN("\uD83D\uDCDA", "Learn trading skills", "Understand markets and strategies"),
    GROW("\uD83C\uDF3F", "Grow my portfolio", "Steady portfolio appreciation"),
    ACTIVE_INCOME("\uD83D\uDCB0", "Generate active income", "Regular profits from trading"),
    LONG_TERM("\uD83C\uDFE6", "Build long-term wealth", "Strategic long-term investments")
}

enum class LearningStyle(
    val emoji: String,
    val displayName: String,
    val subtitle: String
) {
    TEACH_ME("\uD83C\uDF93", "Teach me everything", "Detailed explanations and education"),
    SOME_TIPS("\uD83D\uDCA1", "Some tips along the way", "Occasional insights and tips"),
    JUST_SIGNALS("\uD83D\uDCE1", "Just the signals", "Skip explanations, show me data")
}

data class OnboardingSurvey(
    val experienceLevel: ExperienceLevel,
    val timeAvailability: TimeAvailability,
    val riskComfort: RiskComfort,
    val primaryGoal: PrimaryGoal,
    val learningStyle: LearningStyle
)
