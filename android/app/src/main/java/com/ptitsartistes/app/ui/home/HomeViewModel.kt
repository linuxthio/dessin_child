package com.ptitsartistes.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.dto.DessinDto
import com.ptitsartistes.app.data.repository.DessinRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val dessinRepository: DessinRepository) : ViewModel() {

    var topDessins by mutableStateOf<List<DessinDto>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            isLoading = true
            when (val result = dessinRepository.listTopDessins()) {
                is ApiResult.Success -> topDessins = result.data
                is ApiResult.Error -> Unit // écran d'accueil : on affiche simplement la section vide
            }
            isLoading = false
        }
    }
}
