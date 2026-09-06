package com.ptitsartistes.app.ui.enfant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class EnfantPinViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var pin by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun addDigit(digit: Char, enfantId: Int, onSuccess: () -> Unit) {
        if (pin.length >= 4 || isLoading) return
        errorMessage = null
        pin += digit
        if (pin.length == 4) {
            submit(enfantId, onSuccess)
        }
    }

    fun backspace() {
        if (pin.isNotEmpty()) pin = pin.dropLast(1)
        errorMessage = null
    }

    private fun submit(enfantId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = authRepository.loginEnfant(enfantId, pin)) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.Error -> {
                    errorMessage = result.message
                    pin = ""
                }
            }
            isLoading = false
        }
    }
}
