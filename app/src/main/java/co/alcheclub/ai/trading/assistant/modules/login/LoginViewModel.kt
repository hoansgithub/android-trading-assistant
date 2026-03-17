package co.alcheclub.ai.trading.assistant.modules.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.alcheclub.ai.trading.assistant.domain.model.AuthProvider
import co.alcheclub.ai.trading.assistant.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _state.value = LoginState.Authenticating(AuthProvider.GOOGLE)

            val result = authRepository.signInWithGoogle(activity)

            result.fold(
                onSuccess = {
                    _state.value = LoginState.Authenticated
                },
                onFailure = { error ->
                    val message = error.message ?: "Sign-in failed. Please try again."
                    // Don't show error for user cancellation
                    if (message == "Sign-in was cancelled") {
                        _state.value = LoginState.Idle
                    } else {
                        _state.value = LoginState.Error(message)
                    }
                }
            )
        }
    }
}

sealed class LoginState {
    data object Idle : LoginState()
    data class Authenticating(val provider: AuthProvider) : LoginState()
    data object Authenticated : LoginState()
    data class Error(val message: String) : LoginState()

    val isLoading get() = this is Authenticating
}
