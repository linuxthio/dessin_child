package com.ptitsartistes.app.ui.parent

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.dto.EnfantDto
import com.ptitsartistes.app.data.repository.DessinRepository
import com.ptitsartistes.app.data.repository.EnfantRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class DessinUploadParentViewModel(
    private val enfantRepository: EnfantRepository,
    private val dessinRepository: DessinRepository,
    preselectedEnfantId: Int?,
) : ViewModel() {

    var enfants by mutableStateOf<List<EnfantDto>>(emptyList())
        private set
    var selectedEnfantId by mutableStateOf(preselectedEnfantId)
    var titre by mutableStateOf("")
    var dateCreation by mutableStateOf(LocalDate.now().toString())
    var description by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)

    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    init {
        viewModelScope.launch {
            isLoading = true
            when (val result = enfantRepository.listEnfants()) {
                is ApiResult.Success -> {
                    enfants = result.data
                    if (selectedEnfantId == null) selectedEnfantId = result.data.firstOrNull()?.id
                }
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun save(context: Context, onSuccess: () -> Unit) {
        val enfantId = selectedEnfantId
        when {
            enfantId == null -> errorMessage = "Merci de choisir un enfant."
            titre.isBlank() -> errorMessage = "Merci de donner un titre au dessin."
            imageUri == null -> errorMessage = "Merci de choisir une image."
            else -> viewModelScope.launch {
                isSaving = true
                errorMessage = null
                val result = dessinRepository.createDessinParent(
                    context, enfantId, titre.trim(), dateCreation, description.ifBlank { null }, imageUri!!,
                )
                when (result) {
                    is ApiResult.Success -> onSuccess()
                    is ApiResult.Error -> errorMessage = result.message
                }
                isSaving = false
            }
        }
    }
}
