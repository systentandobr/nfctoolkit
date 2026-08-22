package com.systentando.nfc.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systentando.nfc.data.local.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    fun markOnboardingSeen() {
        viewModelScope.launch {
            onboardingRepository.setOnboardingSeen()
        }
    }
}
