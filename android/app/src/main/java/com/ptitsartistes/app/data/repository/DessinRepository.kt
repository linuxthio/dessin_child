package com.ptitsartistes.app.data.repository

import android.content.Context
import android.net.Uri
import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.ApiService
import com.ptitsartistes.app.data.remote.dto.DessinDto
import com.ptitsartistes.app.data.remote.dto.DessinRatingUpdate
import com.ptitsartistes.app.data.remote.safeApiCall
import com.ptitsartistes.app.data.remote.toTextPart
import com.ptitsartistes.app.data.remote.uriToImagePart

class DessinRepository(private val api: ApiService) {

    // --- Côté parent ------------------------------------------------------

    suspend fun listDessins(enfantId: Int? = null): ApiResult<List<DessinDto>> =
        safeApiCall { api.listDessins(enfantId) }

    /** Top 5 des dessins les mieux notés, public (utilisé sur l'accueil). */
    suspend fun listTopDessins(): ApiResult<List<DessinDto>> = safeApiCall { api.listTopDessins() }

    suspend fun createDessinParent(
        context: Context,
        enfantId: Int,
        titre: String,
        dateCreation: String,
        description: String?,
        imageUri: Uri,
    ): ApiResult<DessinDto> = safeApiCall {
        api.createDessinParent(
            enfant = enfantId.toString().toTextPart(),
            titre = titre.toTextPart(),
            dateCreation = dateCreation.toTextPart(),
            description = description?.toTextPart(),
            image = uriToImagePart(context, imageUri, "image"),
        )
    }

    suspend fun deleteDessin(id: Int): ApiResult<Unit> = safeApiCall { api.deleteDessin(id) }

    suspend fun rateDessinParent(id: Int, note: Int): ApiResult<DessinDto> =
        safeApiCall { api.rateDessinParent(id, DessinRatingUpdate(note = note)) }

    suspend fun toggleLikeParent(id: Int, aime: Boolean): ApiResult<DessinDto> =
        safeApiCall { api.rateDessinParent(id, DessinRatingUpdate(aime = aime)) }

    // --- Côté enfant --------------------------------------------------------

    suspend fun listDessinsEnfant(): ApiResult<List<DessinDto>> = safeApiCall { api.listDessinsEnfant() }

    suspend fun createDessinEnfant(
        context: Context,
        titre: String,
        imageUri: Uri,
    ): ApiResult<DessinDto> = safeApiCall {
        api.createDessinEnfant(
            titre = titre.toTextPart(),
            image = uriToImagePart(context, imageUri, "image"),
        )
    }

    suspend fun deleteDessinEnfant(id: Int): ApiResult<Unit> = safeApiCall { api.deleteDessinEnfant(id) }

    suspend fun rateDessinEnfant(id: Int, note: Int): ApiResult<DessinDto> =
        safeApiCall { api.rateDessinEnfant(id, DessinRatingUpdate(note = note)) }

    suspend fun toggleLikeEnfant(id: Int, aime: Boolean): ApiResult<DessinDto> =
        safeApiCall { api.rateDessinEnfant(id, DessinRatingUpdate(aime = aime)) }
}
