package com.ptitsartistes.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ParentAuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Merci de renseigner votre email et votre mot de passe."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = authRepository.loginParent(email.trim(), password)) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun register(username: String, email: String, password: String, passwordConfirm: String, onSuccess: () -> Unit) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Merci de remplir tous les champs."
            return
        }
        if (password != passwordConfirm) {
            errorMessage = "Les mots de passe ne correspondent pas."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = authRepository.registerParent(username.trim(), email.trim(), password)) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }
}
