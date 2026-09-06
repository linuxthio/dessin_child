package com.ptitsartistes.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParentDto(
    val id: Int,
    val username: String,
    val email: String,
)

@Serializable
data class ParentAuthResponse(
    val token: String,
    val parent: ParentDto,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class EnfantDto(
    val id: Int,
    val prenom: String,
    val nom: String? = null,
    @SerialName("date_naissance") val dateNaissance: String? = null,
    val age: Int? = null,
    val avatar: String? = null,
    @SerialName("pin_code") val pinCode: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class EnfantLoginRequest(
    @SerialName("enfant_id") val enfantId: Int,
    @SerialName("pin_code") val pinCode: String,
)

@Serializable
data class EnfantLoginResponse(
    @SerialName("enfant_token") val enfantToken: String,
    val enfant: EnfantDto,
)

@Serializable
data class DessinDto(
    val id: Int,
    val titre: String,
    val image: String,
    @SerialName("date_creation") val dateCreation: String,
    val description: String? = null,
    val enfant: Int? = null,
    @SerialName("enfant_prenom") val enfantPrenom: String? = null,
    @SerialName("ajoute_par") val ajoutePar: String,
    val note: Int? = null,
    val aime: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class DessinRatingUpdate(
    val note: Int? = null,
    val aime: Boolean? = null,
)

@Serializable
data class ErrorResponse(
    val detail: String? = null,
)
