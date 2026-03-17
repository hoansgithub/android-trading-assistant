package co.alcheclub.ai.trading.assistant.modules.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.alcheclub.ai.trading.assistant.R
import co.alcheclub.ai.trading.assistant.ui.theme.AppDimens
import co.alcheclub.ai.trading.assistant.ui.theme.BgPrimary
import co.alcheclub.ai.trading.assistant.ui.theme.BgSecondary
import co.alcheclub.ai.trading.assistant.ui.theme.Border
import co.alcheclub.ai.trading.assistant.ui.theme.Emerald
import co.alcheclub.ai.trading.assistant.ui.theme.PoppinsFontFamily
import co.alcheclub.ai.trading.assistant.ui.theme.TextSecondary

data class TabItem(
    val title: String,
    val iconRes: Int
)

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    strategyViewModel: StrategyViewModel,
    onLogout: () -> Unit
) {
    val dimens = AppDimens.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            TabItem("My Analyses", R.drawable.ic_tab_home),
            TabItem("Strategy", R.drawable.ic_tab_strategy),
            TabItem("Profile", R.drawable.ic_tab_profile)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> HomeTab(viewModel = homeViewModel)
                1 -> StrategyTab(viewModel = strategyViewModel)
                2 -> ProfileTab(onLogout = onLogout)
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Border)
        )

        // Bottom tab bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSecondary)
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                val tintColor by animateColorAsState(
                    targetValue = if (isSelected) Emerald else TextSecondary,
                    animationSpec = tween(200),
                    label = "tabTint"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp),
                        tint = tintColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tab.title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = tintColor
                    )
                }
            }
        }
    }
}
