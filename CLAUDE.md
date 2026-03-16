# Alpha Profit AI - Android Trading Assistant

## Project Overview
Android counterpart of the iOS app "Alpha Profit AI" (co.alcheclub.ai.trading.assistant).
AI-powered trading chart analysis app with camera capture, strategy builder, and portfolio insights.

## Architecture
- **Pattern:** Clean Architecture with MVVM (following android-short-drama-app patterns)
- **UI Framework:** Jetpack Compose with Material 3
- **Theme:** Dark mode only, emerald green (#2EDBA3) primary accent
- **Font:** Poppins (Regular, Medium, SemiBold, Bold)
- **Activities:** AppCompatActivity base with enableEdgeToEdge()
- **Dimensions:** Scalable system based on 393dp base width (iPhone 15 Pro design frame)

## Package Structure
```
co.alcheclub.ai.trading.assistant/
├── core/           # Shared utilities, extensions, services
├── data/           # Data layer (repositories, API clients, DTOs)
├── di/             # Dependency injection modules
├── domain/         # Domain layer (models, use cases, repository interfaces)
├── modules/        # Feature screens (Compose)
├── navigation/     # Navigation routes
├── ui/
│   ├── components/ # Reusable Compose components
│   └── theme/      # Color, Type, Theme, Dimens
├── MainActivity.kt
└── AlphaProfitApplication.kt
```

## Theme Files
- `ui/theme/Color.kt` - All color definitions (iOS palette: bg #0B0F0E, emerald #2EDBA3)
- `ui/theme/Type.kt` - Poppins typography with scalable sizes
- `ui/theme/Dimens.kt` - Screen-scaled dimensions (font, spacing, radius)
- `ui/theme/Theme.kt` - AlphaProfitTheme composable (dark only)

## iOS Reference
- Source: `/Users/hoanl/Documents/alcheclub/ai-trading-assistant`
- Screens: Splash → Login → Onboarding (8 steps) → Main Tabs (Home/Strategy/Profile)
- Features: Chart photo analysis (AI), strategy builder, authentication (Google/Apple)

## Android Reference
- Architecture patterns from: `/Users/hoanl/Documents/alcheclub-android/android-short-drama-app`
- Follow same folder structure, theme architecture, and Compose patterns

## Build
- compileSdk: 36, minSdk: 28, targetSdk: 36
- Java 11 compatibility
- Gradle version catalog (libs.versions.toml)
