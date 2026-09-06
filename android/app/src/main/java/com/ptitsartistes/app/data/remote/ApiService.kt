package com.ptitsartistes.app.data.remote

import com.ptitsartistes.app.data.remote.dto.DessinDto
import com.ptitsartistes.app.data.remote.dto.DessinRatingUpdate
import com.ptitsartistes.app.data.remote.dto.EnfantDto
import com.ptitsartistes.app.data.remote.dto.EnfantLoginRequest
import com.ptitsartistes.app.data.remote.dto.EnfantLoginResponse
import com.ptitsartistes.app.data.remote.dto.LoginRequest
import com.ptitsartistes.app.data.remote.dto.ParentAuthResponse
import com.ptitsartistes.app.data.remote.dto.ParentDto
import com.ptitsartistes.app.data.remote.dto.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Toutes les routes sont préfixées par `api/v1/` côté serveur Django.
 * L'authentification (Token parent / Enfant-Token) est injectée par
 * [AuthInterceptor] en fonction du chemin de la requête, voir
 * [DynamicBaseUrlInterceptor] pour la résolution de l'hôte.
 */
interface ApiService {

    // --- Authentification parent ---------------------------------------

    @POST("api/v1/auth/register/")
    suspend fun registerParent(@Body body: RegisterRequest): ParentAuthResponse

    @POST("api/v1/auth/login/")
    suspend fun loginParent(@Body body: LoginRequest): ParentAuthResponse

    @POST("api/v1/auth/logout/")
    suspend fun logoutParent()

    @GET("api/v1/auth/me/")
    suspend fun getMe(): ParentDto

    // --- Enfants (gérés par le parent) ----------------------------------

    @GET("api/v1/enfants/")
    suspend fun listEnfants(): List<EnfantDto>

    @Multipart
    @POST("api/v1/enfants/")
    suspend fun createEnfant(
        @Part("prenom") prenom: RequestBody,
        @Part("nom") nom: RequestBody?,
        @Part("date_naissance") dateNaissance: RequestBody?,
        @Part avatar: MultipartBody.Part?,
    ): EnfantDto

    @Multipart
    @PATCH("api/v1/enfants/{id}/")
    suspend fun updateEnfant(
        @Path("id") id: Int,
        @Part("prenom") prenom: RequestBody,
        @Part("nom") nom: RequestBody?,
        @Part("date_naissance") dateNaissance: RequestBody?,
        @Part avatar: MultipartBody.Part?,
    ): EnfantDto

    @DELETE("api/v1/enfants/{id}/")
    suspend fun deleteEnfant(@Path("id") id: Int)

    @GET("api/v1/enfants/public/")
    suspend fun listEnfantsPublic(): List<EnfantDto>

    // --- Dessins côté parent ---------------------------------------------

    @GET("api/v1/dessins/")
    suspend fun listDessins(@Query("enfant") enfantId: Int? = null): List<DessinDto>

    @GET("api/v1/dessins/top/")
    suspend fun listTopDessins(): List<DessinDto>

    @PATCH("api/v1/dessins/{id}/")
    suspend fun rateDessinParent(@Path("id") id: Int, @Body body: DessinRatingUpdate): DessinDto

    @Multipart
    @POST("api/v1/dessins/")
    suspend fun createDessinParent(
        @Part("enfant") enfant: RequestBody,
        @Part("titre") titre: RequestBody,
        @Part("date_creation") dateCreation: RequestBody,
        @Part("description") description: RequestBody?,
        @Part image: MultipartBody.Part,
    ): DessinDto

    @DELETE("api/v1/dessins/{id}/")
    suspend fun deleteDessin(@Path("id") id: Int)

    // --- Authentification enfant (PIN) -----------------------------------

    @POST("api/v1/enfant/login/")
    suspend fun loginEnfant(@Body body: EnfantLoginRequest): EnfantLoginResponse

    @POST("api/v1/enfant/logout/")
    suspend fun logoutEnfant()

    @GET("api/v1/enfant/me/")
    suspend fun getEnfantMe(): EnfantDto

    // --- Dessins côté enfant -----------------------------------------------

    @GET("api/v1/enfant/dessins/")
    suspend fun listDessinsEnfant(): List<DessinDto>

    @Multipart
    @POST("api/v1/enfant/dessins/")
    suspend fun createDessinEnfant(
        @Part("titre") titre: RequestBody,
        @Part image: MultipartBody.Part,
    ): DessinDto

    @PATCH("api/v1/enfant/dessins/{id}/")
    suspend fun rateDessinEnfant(@Path("id") id: Int, @Body body: DessinRatingUpdate): DessinDto

    @DELETE("api/v1/enfant/dessins/{id}/")
    suspend fun deleteDessinEnfant(@Path("id") id: Int)
}
