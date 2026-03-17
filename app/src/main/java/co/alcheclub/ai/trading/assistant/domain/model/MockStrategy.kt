package co.alcheclub.ai.trading.assistant.domain.model

data class MockStrategy(
    val name: String,
    val style: String,
    val timeframe: String,
    val riskPerTrade: String,
    val direction: String
) {
    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun generateFromSurvey(survey: OnboardingSurvey): MockStrategy {
            return MockStrategy(
                name = "Steady Grower",
                style = "Swing Trading",
                timeframe = "4H - Daily",
                riskPerTrade = "1-2%",
                direction = "Long Bias"
            )
        }
    }
}
