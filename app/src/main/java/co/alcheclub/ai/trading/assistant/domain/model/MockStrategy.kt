package co.alcheclub.ai.trading.assistant.domain.model

/**
 * Strategy preset generated from onboarding survey.
 * Matches iOS GenerateStrategyPresetUseCase logic with 10 distinct archetypes.
 */
data class MockStrategy(
    val name: String,
    val style: String,
    val timeframe: String,
    val riskPerTrade: String,
    val direction: String
) {
    companion object {
        fun generateFromSurvey(survey: OnboardingSurvey): MockStrategy {
            val preset = resolvePreset(survey.primaryGoal, survey.timeAvailability, survey.riskComfort)
            val style = resolveStyle(survey.timeAvailability, survey.primaryGoal)
            val timeframe = resolveTimeframe(survey.timeAvailability, survey.primaryGoal)
            val (riskPercent, direction) = resolveRisk(survey.riskComfort, survey.primaryGoal)

            return MockStrategy(
                name = preset,
                style = style,
                timeframe = timeframe,
                riskPerTrade = riskPercent,
                direction = direction
            )
        }

        /**
         * 10 preset archetypes based on goal × time × risk (matching iOS).
         */
        private fun resolvePreset(
            goal: PrimaryGoal,
            time: TimeAvailability,
            risk: RiskComfort
        ): String = when (goal) {
            PrimaryGoal.LEARN -> "Safe Starter"
            PrimaryGoal.GROW -> when (time) {
                TimeAvailability.WEEKLY, TimeAvailability.DAILY -> "Steady Grower"
                TimeAvailability.FEW_PER_DAY, TimeAvailability.HOURLY -> "Momentum Builder"
            }
            PrimaryGoal.ACTIVE_INCOME -> when {
                risk == RiskComfort.CONSERVATIVE -> "Disciplined Day Trader"
                risk == RiskComfort.MODERATE -> "Active Trader"
                time == TimeAvailability.HOURLY -> "Quick Scalper"
                else -> "Aggressive Day Trader"
            }
            PrimaryGoal.LONG_TERM -> when (risk) {
                RiskComfort.CONSERVATIVE -> "Long-Term Investor"
                RiskComfort.MODERATE, RiskComfort.AGGRESSIVE -> "Position Trader"
            }
        }

        private fun resolveStyle(time: TimeAvailability, goal: PrimaryGoal): String = when {
            goal == PrimaryGoal.LONG_TERM && time == TimeAvailability.WEEKLY -> "Investing"
            goal == PrimaryGoal.LONG_TERM -> "Position Trading"
            time == TimeAvailability.WEEKLY -> "Swing Trading"
            time == TimeAvailability.DAILY -> "Swing Trading"
            time == TimeAvailability.FEW_PER_DAY -> "Day Trading"
            time == TimeAvailability.HOURLY && goal == PrimaryGoal.ACTIVE_INCOME -> "Scalping"
            time == TimeAvailability.HOURLY -> "Day Trading"
            else -> "Swing Trading"
        }

        private fun resolveTimeframe(time: TimeAvailability, goal: PrimaryGoal): String = when {
            goal == PrimaryGoal.LEARN -> when (time) {
                TimeAvailability.WEEKLY -> "1 Day"
                TimeAvailability.DAILY -> "4 Hours"
                TimeAvailability.FEW_PER_DAY -> "1 Hour"
                TimeAvailability.HOURLY -> "30 Min"
            }
            goal == PrimaryGoal.LONG_TERM -> when (time) {
                TimeAvailability.WEEKLY -> "1 Week"
                else -> "1 Day"
            }
            else -> when (time) {
                TimeAvailability.WEEKLY -> "4 Hours"
                TimeAvailability.DAILY -> "1 Hour"
                TimeAvailability.FEW_PER_DAY -> "15 Min"
                TimeAvailability.HOURLY -> "5 Min"
            }
        }

        private fun resolveRisk(risk: RiskComfort, goal: PrimaryGoal): Pair<String, String> = when (risk) {
            RiskComfort.CONSERVATIVE -> {
                val pct = if (goal == PrimaryGoal.LEARN) "0.5%" else "1.0%"
                pct to "Long Only"
            }
            RiskComfort.MODERATE -> {
                val dir = if (goal == PrimaryGoal.LONG_TERM) "Long Only" else "Long & Short"
                "2.0%" to dir
            }
            RiskComfort.AGGRESSIVE -> "3.0%" to "Long & Short"
        }
    }
}
