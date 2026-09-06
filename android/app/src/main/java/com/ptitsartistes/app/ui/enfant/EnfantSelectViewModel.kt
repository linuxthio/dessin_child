package com.ptitsartistes.app.ui.enfant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.dto.EnfantDto
import com.ptitsartistes.app.data.repository.EnfantRepository
import kotlinx.coroutines.launch

class EnfantSelectViewModel(private val enfantRepository: EnfantRepository) : ViewModel() {

    var enfants by mutableStateOf<List<EnfantDto>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = enfantRepository.listEnfantsPublic()) {
                is ApiResult.Success -> enfants = result.data
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }
}
