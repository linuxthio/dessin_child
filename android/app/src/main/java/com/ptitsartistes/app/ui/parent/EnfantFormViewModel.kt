package com.ptitsartistes.app.ui.parent

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.repository.EnfantRepository
import kotlinx.coroutines.launch

class EnfantFormViewModel(
    private val enfantRepository: EnfantRepository,
    private val enfantId: Int?,
) : ViewModel() {

    val isEditing: Boolean get() = enfantId != null

    var prenom by mutableStateOf("")
    var nom by mutableStateOf("")
    var dateNaissance by mutableStateOf("")
    var avatarUri by mutableStateOf<Uri?>(null)
    var existingAvatarUrl by mutableStateOf<String?>(null)
    var pinCode by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    init {
        if (enfantId != null) loadExisting(enfantId)
    }

    private fun loadExisting(id: Int) {
        viewModelScope.launch {
            isLoading = true
            when (val result = enfantRepository.listEnfants()) {
                is ApiResult.Success -> {
                    result.data.find { it.id == id }?.let { enfant ->
                        prenom = enfant.prenom
                        nom = enfant.nom.orEmpty()
                        dateNaissance = enfant.dateNaissance.orEmpty()
                        existingAvatarUrl = enfant.avatar
                        pinCode = enfant.pinCode
                    }
                }
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun save(context: Context, onSuccess: () -> Unit) {
        if (prenom.isBlank()) {
            errorMessage = "Le prénom est obligatoire."
            return
        }
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            val result = if (enfantId != null) {
                enfantRepository.updateEnfant(
                    context, enfantId, prenom.trim(), nom.trim().ifBlank { null },
                    dateNaissance.ifBlank { null }, avatarUri,
                )
            } else {
                enfantRepository.createEnfant(
                    context, prenom.trim(), nom.trim().ifBlank { null },
                    dateNaissance.ifBlank { null }, avatarUri,
                )
            }
            when (result) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.Error -> errorMessage = result.message
            }
            isSaving = false
        }
    }
}
