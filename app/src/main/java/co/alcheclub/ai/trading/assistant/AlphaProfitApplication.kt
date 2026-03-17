package co.alcheclub.ai.trading.assistant

import android.app.Application
import co.alcheclub.ai.trading.assistant.di.AppModule

/**
 * Application class for Alpha Profit AI.
 * Initializes DI and global services.
 */
class AlphaProfitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize manual DI
        AppModule.init(this)

        // TODO: Initialize analytics
        // TODO: Initialize remote config
    }
}
