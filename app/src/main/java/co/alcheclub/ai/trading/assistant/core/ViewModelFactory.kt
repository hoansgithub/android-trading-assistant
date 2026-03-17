package co.alcheclub.ai.trading.assistant.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Generic ViewModelProvider.Factory for manual DI.
 *
 * Usage:
 * ```
 * private val viewModel: MyViewModel by viewModels {
 *     viewModelFactory { MyViewModel(dep1, dep2) }
 * }
 * ```
 *
 * This ensures ViewModels survive configuration changes (rotation, theme switch)
 * and are properly scoped to the Activity/Fragment lifecycle.
 */
inline fun <reified T : ViewModel> viewModelFactory(
    crossinline creator: () -> T
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return creator() as VM
    }
}
