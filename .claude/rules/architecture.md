# Clean Architecture Rules

## Layer Independence
```kotlin
// FORBIDDEN - Domain depends on Data layer
import com.app.core.data.local.LanguageManager  // Data layer!

class CheckLanguageUseCase(
    private val languageManager: LanguageManager  // Concrete class!
)

// REQUIRED - Domain depends on interface
// domain/repository/LanguageRepository.kt
interface LanguageRepository {
    fun isLanguageSelected(): Boolean
}

// data/local/LanguageManager.kt
class LanguageManager(context: Context) : LanguageRepository {
    override fun isLanguageSelected(): Boolean = ...
}

// domain/usecase/CheckLanguageUseCase.kt
class CheckLanguageUseCase(
    private val repository: LanguageRepository  // Interface!
)
```

## ACCDI Module Pattern
```kotlin
val dataModule = module {
    single { PreferencesManager(androidContext()) }
    single<SeriesRepository> { SeriesRepositoryImpl(it.get()) }
}

val domainModule = module {
    factory { GetSeriesUseCase(it.get()) }
}

val presentationModule = module {
    viewModel { HomeViewModel(it.get()) }
}
```

## Repository Pattern
```kotlin
// Interface in Domain layer
interface FeatureRepository {
    suspend fun getData(): Result<Data>
}

// Implementation in Data layer
class FeatureRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : FeatureRepository {
    override suspend fun getData(): Result<Data> {
        return try {
            val data = supabaseClient.from("table").select().decodeList<Data>()
            Result.success(data)
        } catch (e: Exception) {
            Log.e("Repository", "Failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
```
