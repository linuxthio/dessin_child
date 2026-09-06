package com.ptitsartistes.app.ui.enfant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.dto.DessinDto
import com.ptitsartistes.app.data.repository.AuthRepository
import com.ptitsartistes.app.data.repository.DessinRepository
import kotlinx.coroutines.launch

class EnfantDashboardViewModel(
    private val dessinRepository: DessinRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var dessins by mutableStateOf<List<DessinDto>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = dessinRepository.listDessinsEnfant()) {
                is ApiResult.Success -> dessins = result.data
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun deleteDessin(id: Int) {
        viewModelScope.launch {
            when (val result = dessinRepository.deleteDessinEnfant(id)) {
                is ApiResult.Success -> refresh()
                is ApiResult.Error -> errorMessage = result.message
            }
        }
    }

    fun rateDessin(id: Int, note: Int) {
        viewModelScope.launch {
            errorMessage = null
            when (val result = dessinRepository.rateDessinEnfant(id, note)) {
                is ApiResult.Success -> replaceDessin(result.data)
                is ApiResult.Error -> errorMessage = result.message
            }
        }
    }

    fun toggleLike(id: Int) {
        val current = dessins.find { it.id == id } ?: return
        viewModelScope.launch {
            errorMessage = null
            when (val result = dessinRepository.toggleLikeEnfant(id, !current.aime)) {
                is ApiResult.Success -> replaceDessin(result.data)
                is ApiResult.Error -> errorMessage = result.message
            }
        }
    }

    private fun replaceDessin(updated: DessinDto) {
        dessins = dessins.map { if (it.id == updated.id) updated else it }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logoutEnfant()
            onDone()
        }
    }
}
