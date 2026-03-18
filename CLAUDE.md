# Alpha Profit AI - Android Trading Assistant

## Project Overview
Android counterpart of the iOS app "Alpha Profit AI" (co.alcheclub.ai.trading.assistant).
AI-powered trading chart analysis app with camera capture, strategy builder, and portfolio insights.

## References

### Architecture Reference (Android patterns)
- **Path:** `/Users/hoanl/Documents/alcheclub-android/android-short-drama-app`
- **Use for:** Folder structure, activity patterns, theme architecture, Compose patterns, DI module structure, navigation patterns, ViewModel patterns, data layer patterns
- Follow same Clean Architecture + MVVM approach

### Logic & Resources Reference (iOS app)
- **Path:** `/Users/hoanl/Documents/alcheclub/ai-trading-assistant`
- **Use for:** App logic, screen flows, UI design, colors, assets, feature requirements, business rules
- All screens, flows, and features should match this iOS app
- Screens: Splash → Login → Onboarding (9 steps) → Main Tabs (Home/Strategy/Profile)
- Features: Chart photo analysis (AI), strategy builder, authentication (Google/Apple)

## Architecture
- **Pattern:** Clean Architecture with MVVM
- **UI Framework:** Jetpack Compose with Material 3
- **Theme:** Dark mode only, emerald green (#2EDBA3) primary accent
- **Font:** Poppins (Regular, Medium, SemiBold, Bold)
- **Activities:** AppCompatActivity base with enableEdgeToEdge() + SplashScreen API
- **Dimensions:** Scalable system based on 393dp base width (iPhone 15 Pro design frame)
- **DI:** Manual DI via AppModule (will migrate to Hilt/ACCCore later)

## Package Structure
```
co.alcheclub.ai.trading.assistant/
├── core/extensions/    # ActivityExtensions (fade transition)
├── data/
│   ├── local/          # PreferencesManager (SharedPreferences)
│   └── repository/     # Repository implementations
├── di/                 # AppModule (manual DI)
├── domain/
│   ├── model/          # OnboardingSurvey enums, AuthProvider, MockStrategy
│   └── repository/     # Repository interfaces
├── modules/
│   ├── login/          # LoginActivity, LoginScreen, LoginViewModel
│   ├── onboarding/     # OnboardingActivity, Screen, ViewModel, steps/, components/
│   └── root/           # LoadingScreen, RootNavigationEvent
├── ui/theme/           # Color, Type, Theme, Dimens
├── RootActivity.kt     # Entry point (splash → routing)
├── MainActivity.kt     # Main app content
└── AlphaProfitApplication.kt
```

## Theme Files
- `ui/theme/Color.kt` - All color definitions (iOS palette: bg #0B0F0E, emerald #2EDBA3)
- `ui/theme/Type.kt` - Poppins typography with scalable sizes
- `ui/theme/Dimens.kt` - Screen-scaled dimensions (font, spacing, radius)
- `ui/theme/Theme.kt` - AlphaProfitTheme composable (dark only)

## Activity Flow
RootActivity (launcher + splash) → LoginActivity → OnboardingActivity → MainActivity

### Database Query Safety (CRITICAL) @.claude/rules/database-safety.md
- NEVER fetch all records from a database (assume billions of rows)
- ALL queries MUST have LIMIT / pagination
- ALL filtering MUST be in the query (WHERE clauses), NOT client-side
- ALL sorting MUST be in the query (ORDER BY), NOT client-side
- Applies to: Room, Supabase, Firebase, SQL, MongoDB, etc.

## Build
- compileSdk: 36, minSdk: 28, targetSdk: 36
- Java 17 compatibility
- AGP 8.13.2, Kotlin 2.1.0
- Gradle version catalog (libs.versions.toml)
