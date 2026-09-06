package com.ptitsartistes.app.data.repository

import android.content.Context
import android.net.Uri
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.ApiService
import com.ptitsartistes.app.data.remote.dto.EnfantDto
import com.ptitsartistes.app.data.remote.safeApiCall
import com.ptitsartistes.app.data.remote.toTextPart
import com.ptitsartistes.app.data.remote.uriToImagePart

class EnfantRepository(private val api: ApiService) {

    suspend fun listEnfants(): ApiResult<List<EnfantDto>> = safeApiCall { api.listEnfants() }

    suspend fun listEnfantsPublic(): ApiResult<List<EnfantDto>> = safeApiCall { api.listEnfantsPublic() }

    suspend fun createEnfant(
        context: Context,
        prenom: String,
        nom: String?,
        dateNaissance: String?,
        avatarUri: Uri?,
    ): ApiResult<EnfantDto> = safeApiCall {
        api.createEnfant(
            prenom = prenom.toTextPart(),
            nom = nom?.toTextPart(),
            dateNaissance = dateNaissance?.toTextPart(),
            avatar = avatarUri?.let { uriToImagePart(context, it, "avatar") },
        )
    }

    suspend fun updateEnfant(
        context: Context,
        id: Int,
        prenom: String,
        nom: String?,
        dateNaissance: String?,
        avatarUri: Uri?,
    ): ApiResult<EnfantDto> = safeApiCall {
        api.updateEnfant(
            id = id,
            prenom = prenom.toTextPart(),
            nom = nom?.toTextPart(),
            dateNaissance = dateNaissance?.toTextPart(),
            avatar = avatarUri?.let { uriToImagePart(context, it, "avatar") },
        )
    }

    suspend fun deleteEnfant(id: Int): ApiResult<Unit> = safeApiCall { api.deleteEnfant(id) }
}
