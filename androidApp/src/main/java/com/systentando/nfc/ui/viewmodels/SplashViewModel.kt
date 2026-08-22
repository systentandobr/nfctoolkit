package com.systentando.nfc.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systentando.nfc.data.local.OnboardingRepository
import com.systentando.nfc.domain.model.NFCTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val seen = onboardingRepository.isOnboardingSeen()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                onboardingSeen = seen
            )
        }
    }

    fun setPendingTag(tag: NFCTag?) {
        _uiState.value = _uiState.value.copy(pendingTag = tag)
    }

    data class SplashUiState(
        val isLoading: Boolean = true,
        val onboardingSeen: Boolean = false,
        val pendingTag: NFCTag? = null
    )
}
