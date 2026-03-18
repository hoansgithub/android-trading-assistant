package co.alcheclub.ai.trading.assistant.core.analytics

/**
 * Analytics Event Constants
 *
 * Centralized event names and parameters for analytics tracking.
 * Mirrors the iOS AppEvent.swift definitions.
 *
 * Usage:
 * ```kotlin
 * Analytics.track(
 *     name = AnalyticsEvent.ANALYSIS_COMPLETE,
 *     params = mapOf(
 *         AnalyticsEvent.Param.ASSET to "AAPL",
 *         AnalyticsEvent.Param.SIGNAL to "buy",
 *         AnalyticsEvent.Param.MODEL to "flash"
 *     )
 * )
 * ```
 */
object AnalyticsEvent {

    // ============================================
    // PARAMETER KEYS
    // ============================================

    object Param {
        const val SCREEN = "screen"        // Screen/placement where event occurred
        const val VALUE = "value"          // String: event value or answer
        const val SOURCE = "source"        // String: event source (camera, gallery, apple, google)
        const val MODEL = "model"          // String: AI model used (flash, pro)
        const val ASSET = "asset"          // String: asset symbol (BTC, AAPL, etc.)
        const val SIGNAL = "signal"        // String: AI signal (buy, sell, hold)
        const val RISK = "risk"            // String: risk level (low, moderate, high)
        const val ERROR = "error"          // String: error code
        const val STEP = "step"            // String: onboarding step number
        const val STYLE = "style"          // String: trading style (swingTrading, dayTrading, longTerm)
        const val TIMEFRAME = "timeframe"  // String: chart timeframe (m15, m30, h1, h4, d1, w1)
        const val PRODUCT = "product"      // String: subscription product id
    }

    // ============================================
    // AUTHENTICATION EVENTS
    // ============================================

    /** User tapped sign-in button. Param: source (apple | google) */
    const val AUTH_START = "auth_start"

    /** Authentication completed successfully. Param: source (apple | google) */
    const val AUTH_SUCCESS = "auth_success"

    /** Authentication failed. Params: source (apple | google), error (code) */
    const val AUTH_ERROR = "auth_error"

    /** User signed out */
    const val AUTH_SIGN_OUT = "auth_sign_out"

    /** User deleted their account */
    const val AUTH_DELETE_ACCOUNT = "auth_delete_account"

    // ============================================
    // ONBOARDING EVENTS
    // ============================================

    /** User answered an onboarding survey question. Params: step (0-4), value (answer) */
    const val ONBOARDING_STEP = "onboarding_step"

    /** Onboarding completed. Params: style (trading style), timeframe */
    const val ONBOARDING_COMPLETE = "onboarding_complete"

    // ============================================
    // ANALYSIS EVENTS
    // ============================================

    /** User hit free-tier analysis limit */
    const val ANALYSIS_LIMIT = "analysis_limit"

    /** User started a new analysis. Param: source (camera | gallery) */
    const val ANALYSIS_START = "analysis_start"

    /** Chart recognition succeeded. Params: asset, timeframe, value (stock | crypto) */
    const val ANALYSIS_RECOGNITION_OK = "analysis_recognition_ok"

    /** Chart recognition failed. Param: error (code) */
    const val ANALYSIS_RECOGNITION_FAIL = "analysis_recognition_fail"

    /** Market data fetch succeeded. Params: asset, source (binance | alpaca) */
    const val ANALYSIS_MARKET_DATA_OK = "analysis_market_data_ok"

    /** Market data fetch failed. Params: asset, error (code) */
    const val ANALYSIS_MARKET_DATA_FAIL = "analysis_market_data_fail"

    /** AI analysis succeeded. Params: model, signal (buy | sell | hold), risk (low | moderate | high) */
    const val ANALYSIS_AI_OK = "analysis_ai_ok"

    /** AI analysis failed. Params: model, error (code) */
    const val ANALYSIS_AI_FAIL = "analysis_ai_fail"

    /** Analysis persistence to DB failed. Param: error (code) */
    const val ANALYSIS_SAVE_FAIL = "analysis_save_fail"

    /** Chart image upload failed. Param: error (code) */
    const val ANALYSIS_UPLOAD_FAIL = "analysis_upload_fail"

    /** Full analysis pipeline completed. Params: asset, signal (buy | sell | hold), model */
    const val ANALYSIS_COMPLETE = "analysis_complete"

    /** User tapped retry after analysis error */
    const val ANALYSIS_RETRY = "analysis_retry"

    /** User deleted an analysis. Param: asset (symbol) */
    const val ANALYSIS_DELETE = "analysis_delete"

    // ============================================
    // STRATEGY EVENTS
    // ============================================

    /** New strategy created. Params: style, timeframe, value (direction: long | short | both) */
    const val STRATEGY_CREATE = "strategy_create"

    /** Existing strategy edited */
    const val STRATEGY_EDIT = "strategy_edit"

    /** Strategy deleted */
    const val STRATEGY_DELETE = "strategy_delete"

    /** Strategy duplicated */
    const val STRATEGY_DUPLICATE = "strategy_duplicate"

    /** User hit free-tier strategy limit */
    const val STRATEGY_LIMIT = "strategy_limit"

    // ============================================
    // PROFILE EVENTS
    // ============================================

    /** User tapped a profile action. Param: value (terms | privacy | rate | feedback | restore) */
    const val PROFILE_ACTION = "profile_action"

    /** Restore purchases result. Param: value (success | error) */
    const val PROFILE_RESTORE = "profile_restore"

    /** Notifications setting toggled. Param: value (true | false) */
    const val PROFILE_NOTIFICATIONS = "profile_notifications"

    // ============================================
    // PAYWALL EVENTS
    // ============================================

    /** Paywall shown. Param: screen (placement: newAnalysis | strategyCreate | onboarding) */
    const val PAYWALL_SHOW = "paywall_show"

    /** User clicked a product on paywall. Params: screen (placement), product (id) */
    const val PAYWALL_CLICK = "paywall_click"

    /** Purchase completed successfully. Params: screen (placement), product (id) */
    const val PAYWALL_SUCCESS = "paywall_success"

    /** Purchase cancelled. Params: screen (placement), product (id) */
    const val PAYWALL_CANCEL = "paywall_cancel"

    /** Restore purchases tapped on paywall. Param: screen (placement) */
    const val PAYWALL_RESTORE = "paywall_restore"

    /** Purchase or restore error. Params: screen (placement), product (id) */
    const val PAYWALL_ERROR = "paywall_error"

    /** User closed paywall. Param: screen (placement) */
    const val PAYWALL_CLOSE = "paywall_close"

    // ============================================
    // CHART GUIDE EVENTS
    // ============================================

    /** User interacted with chart guide overlay. Param: value (ok | dont_show_again) */
    const val CHART_GUIDE = "chart_guide"

    // ============================================
    // SCREEN NAMES
    // Used with Analytics.trackScreenView()
    // ============================================

    object Screen {
        const val SPLASH = "splash"
        const val LOGIN = "login"
        const val ONBOARDING = "onboarding"
        const val HOME = "home"
        const val STRATEGY = "strategy"
        const val PROFILE = "profile"
        const val CAMERA = "camera"
        const val GALLERY = "gallery"
        const val NEW_ANALYSIS_FLOW = "newAnalysisFlow"
        const val STRATEGY_SELECTION = "strategySelection"
        const val ANALYSIS_PROCESSING = "analysisProcessing"
        const val ANALYSIS_RESULT = "analysisResult"
        const val STRATEGY_DETAIL = "strategyDetail"
        const val STRATEGY_BUILDER = "strategyBuilder"
        const val CHART_GUIDE = "chartGuide"
    }
}