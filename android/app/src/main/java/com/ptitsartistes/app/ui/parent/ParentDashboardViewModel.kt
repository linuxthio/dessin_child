package com.ptitsartistes.app.ui.parent

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.dto.DessinDto
import com.ptitsartistes.app.data.remote.dto.EnfantDto
import com.ptitsartistes.app.data.repository.AuthRepository
import com.ptitsartistes.app.data.repository.DessinRepository
import com.ptitsartistes.app.data.repository.EnfantRepository
import kotlinx.coroutines.launch

class ParentDashboardViewModel(
    private val enfantRepository: EnfantRepository,
    private val dessinRepository: DessinRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var enfants by mutableStateOf<List<EnfantDto>>(emptyList())
        private set
    var dessins by mutableStateOf<List<DessinDto>>(emptyList())
        private set
    var selectedEnfantId by mutableStateOf<Int?>(null)
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

            when (val enfantsResult = enfantRepository.listEnfants()) {
                is ApiResult.Success -> enfants = enfantsResult.data
                is ApiResult.Error -> errorMessage = enfantsResult.message
            }

            when (val dessinsResult = dessinRepository.listDessins(selectedEnfantId)) {
                is ApiResult.Success -> dessins = dessinsResult.data
                is ApiResult.Error -> errorMessage = dessinsResult.message
            }

            isLoading = false
        }
    }

    fun selectEnfant(enfantId: Int?) {
        selectedEnfantId = enfantId
        refresh()
    }

    fun deleteEnfant(id: Int) {
        viewModelScope.launch {
            errorMessage = null
            when (val result = enfantRepository.deleteEnfant(id)) {
                is ApiResult.Success -> {
                    if (selectedEnfantId == id) selectedEnfantId = null
                    refresh()
                }
                is ApiResult.Error -> errorMessage = result.message
            }
        }
    }

    fun deleteDessin(id: Int) {
        viewModelScope.launch {
            errorMessage = null
            when (val result = dessinRepository.deleteDessin(id)) {
                is ApiResult.Success -> refresh()
                is ApiResult.Error -> errorMessage = result.message
            }
        }
    }

    fun rateDessin(id: Int, note: Int) {
        viewModelScope.launch {
            errorMessage = null
            when (val result = dessinRepository.rateDessinParent(id, note)) {
                is ApiResult.Success -> replaceDessin(result.data)
                is ApiResult.Error -> errorMessage = result.message
            }
        }
    }

    fun toggleLike(id: Int) {
        val current = dessins.find { it.id == id } ?: return
        viewModelScope.launch {
            errorMessage = null
            when (val result = dessinRepository.toggleLikeParent(id, !current.aime)) {
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
            authRepository.logoutParent()
            onDone()
        }
    }
}
