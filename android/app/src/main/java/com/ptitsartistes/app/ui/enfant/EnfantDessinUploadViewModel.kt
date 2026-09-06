package com.ptitsartistes.app.ui.enfant

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.repository.DessinRepository
import kotlinx.coroutines.launch

class EnfantDessinUploadViewModel(private val dessinRepository: DessinRepository) : ViewModel() {

    var titre by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)

    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun save(context: Context, onSuccess: () -> Unit) {
        when {
            titre.isBlank() -> errorMessage = "Donne un titre à ton dessin !"
            imageUri == null -> errorMessage = "Choisis d'abord une photo de ton dessin."
            else -> viewModelScope.launch {
                isSaving = true
                errorMessage = null
                when (val result = dessinRepository.createDessinEnfant(context, titre.trim(), imageUri!!)) {
                    is ApiResult.Success -> onSuccess()
                    is ApiResult.Error -> errorMessage = result.message
                }
                isSaving = false
            }
        }
    }
}
