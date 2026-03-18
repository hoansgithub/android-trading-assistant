package co.alcheclub.ai.trading.assistant.di

import android.content.Context
import co.alcheclub.ai.trading.assistant.BuildConfig
import co.alcheclub.ai.trading.assistant.data.local.PreferencesManager
import co.alcheclub.ai.trading.assistant.data.remote.ApiKeyManager
import co.alcheclub.ai.trading.assistant.data.remote.HttpClientProvider
import co.alcheclub.ai.trading.assistant.data.remote.KeyPairManager
import co.alcheclub.ai.trading.assistant.data.remote.SupabaseProvider
import co.alcheclub.ai.trading.assistant.data.remote.service.AlpacaService
import co.alcheclub.ai.trading.assistant.data.remote.service.BinanceService
import co.alcheclub.ai.trading.assistant.data.remote.service.GeminiTextService
import co.alcheclub.ai.trading.assistant.data.remote.service.GeminiVisionService
import co.alcheclub.ai.trading.assistant.data.remote.service.ImageUploadService
import co.alcheclub.ai.trading.assistant.data.repository.AnalysisRepositoryImpl
import co.alcheclub.ai.trading.assistant.data.repository.AuthRepositoryImpl
import co.alcheclub.ai.trading.assistant.data.repository.MarketDataRepositoryImpl
import co.alcheclub.ai.trading.assistant.data.repository.OnboardingRepositoryImpl
import co.alcheclub.ai.trading.assistant.data.repository.StrategyRepositoryImpl
import co.alcheclub.ai.trading.assistant.domain.repository.AnalysisRepository
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import co.alcheclub.ai.trading.assistant.domain.repository.OnboardingRepository
import co.alcheclub.ai.trading.assistant.domain.repository.StrategyRepository
import co.alcheclub.ai.trading.assistant.modules.main.HomeViewModel
import co.alcheclub.ai.trading.assistant.modules.main.ProfileViewModel
import co.alcheclub.ai.trading.assistant.modules.main.StrategyViewModel
import co.alcheclub.ai.trading.assistant.domain.usecase.AnalyzeChartUseCase
import co.alcheclub.ai.trading.assistant.domain.usecase.FetchAnalysesUseCase
import co.alcheclub.ai.trading.assistant.domain.usecase.RecognizeChartUseCase

object AppModule {

    private var preferencesManager: PreferencesManager? = null

    fun init(context: Context) {
        preferencesManager = PreferencesManager(context)
    }

    // region Existing

    val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl(
            preferencesManager = preferencesManager!!,
            supabaseClient = SupabaseProvider.client
        )
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            preferencesManager = preferencesManager!!,
            supabaseClient = SupabaseProvider.client
        )
    }

    // endregion

    // region API Key Managers

    private val geminiKeyManager: ApiKeyManager by lazy {
        ApiKeyManager(
            BuildConfig.GEMINI_API_KEYS.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
    }

    private val binanceKeyManager: ApiKeyManager by lazy {
        ApiKeyManager(
            BuildConfig.BINANCE_API_KEYS.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
    }

    private val alpacaKeyPairManager: KeyPairManager by lazy {
        KeyPairManager(
            keyIds = BuildConfig.ALPACA_API_KEY_ID.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            secretKeys = BuildConfig.ALPACA_API_SECRET_KEY.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
    }

    // endregion

    // region Services (lazy singletons)

    val geminiVisionService: GeminiVisionService by lazy {
        GeminiVisionService(
            client = HttpClientProvider.client,
            keyManager = geminiKeyManager,
            json = HttpClientProvider.json
        )
    }

    private val binanceSpotService: BinanceService by lazy {
        BinanceService(
            client = HttpClientProvider.client,
            keyManager = binanceKeyManager,
            baseUrl = "https://api.binance.com",
            pathPrefix = "/api/v3"
        )
    }

    private val binanceFuturesService: BinanceService by lazy {
        BinanceService(
            client = HttpClientProvider.client,
            keyManager = binanceKeyManager,
            baseUrl = "https://fapi.binance.com",
            pathPrefix = "/fapi/v1"
        )
    }

    private val alpacaService: AlpacaService by lazy {
        AlpacaService(
            client = HttpClientProvider.client,
            keyPairManager = alpacaKeyPairManager
        )
    }

    val geminiTextService: GeminiTextService by lazy {
        GeminiTextService(
            client = HttpClientProvider.client,
            keyManager = geminiKeyManager,
            json = HttpClientProvider.json,
            model = "gemini-2.5-flash",
            maxOutputTokens = 16384,
            thinkingBudget = 1024
        )
    }

    val imageUploadService: ImageUploadService by lazy {
        ImageUploadService(
            client = HttpClientProvider.client,
            supabaseClient = SupabaseProvider.client,
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
        )
    }

    // endregion

    // region Repositories (lazy singletons)

    val marketDataRepository: MarketDataRepositoryImpl by lazy {
        MarketDataRepositoryImpl(
            binanceSpotService = binanceSpotService,
            binanceFuturesService = binanceFuturesService,
            alpacaService = alpacaService
        )
    }

    val analysisRepository: AnalysisRepository by lazy {
        AnalysisRepositoryImpl(supabaseClient = SupabaseProvider.client)
    }

    val strategyRepository: StrategyRepository by lazy {
        StrategyRepositoryImpl(supabaseClient = SupabaseProvider.client)
    }

    // endregion

    // region Use Cases (transient — new instance each call)

    fun createRecognizeChartUseCase(): RecognizeChartUseCase {
        return RecognizeChartUseCase(visionService = geminiVisionService)
    }

    fun createAnalyzeChartUseCase(): AnalyzeChartUseCase {
        return AnalyzeChartUseCase(
            recognizeChartUseCase = createRecognizeChartUseCase(),
            marketDataRepository = marketDataRepository,
            geminiTextService = geminiTextService,
            analysisRepository = analysisRepository,
            imageUploadService = imageUploadService
        )
    }

    fun createFetchAnalysesUseCase(): FetchAnalysesUseCase {
        return FetchAnalysesUseCase(analysisRepository = analysisRepository)
    }

    // endregion

    // region ViewModels (transient)

    fun createHomeViewModel(): HomeViewModel {
        return HomeViewModel(
            analysisRepository = analysisRepository,
            authRepository = authRepository,
            analyzeChartUseCase = createAnalyzeChartUseCase()
        )
    }

    fun createStrategyViewModel(): StrategyViewModel {
        return StrategyViewModel(
            strategyRepository = strategyRepository,
            authRepository = authRepository
        )
    }

    fun createProfileViewModel(): ProfileViewModel {
        return ProfileViewModel(
            authRepository = authRepository,
            supabaseClient = SupabaseProvider.client,
            httpClient = HttpClientProvider.client
        )
    }

    // endregion
}
