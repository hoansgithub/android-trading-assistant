package co.alcheclub.ai.trading.assistant.di

import android.content.Context
import co.alcheclub.ai.trading.assistant.data.local.PreferencesManager
import co.alcheclub.ai.trading.assistant.data.remote.SupabaseProvider
import co.alcheclub.ai.trading.assistant.data.repository.AuthRepositoryImpl
import co.alcheclub.ai.trading.assistant.data.repository.OnboardingRepositoryImpl
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import co.alcheclub.ai.trading.assistant.domain.repository.OnboardingRepository

object AppModule {

    private var preferencesManager: PreferencesManager? = null

    fun init(context: Context) {
        preferencesManager = PreferencesManager(context)
    }

    val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl(preferencesManager!!)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            preferencesManager = preferencesManager!!,
            supabaseClient = SupabaseProvider.client
        )
    }
}
