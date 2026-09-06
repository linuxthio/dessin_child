package com.ptitsartistes.app.data.repository

import com.ptitsartistes.app.data.remote.ApiResult
import com.ptitsartistes.app.data.remote.ApiService
import com.ptitsartistes.app.data.remote.dto.EnfantLoginRequest
import com.ptitsartistes.app.data.remote.dto.LoginRequest
import com.ptitsartistes.app.data.remote.dto.ParentDto
import com.ptitsartistes.app.data.remote.dto.RegisterRequest
import com.ptitsartistes.app.data.remote.safeApiCall
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val api: ApiService,
    private val session: SessionRepository,
) {
    val parentToken: StateFlow<String?> = session.parentToken
    val enfantToken: StateFlow<String?> = session.enfantToken
    val enfantProfile = session.enfantProfile

    suspend fun registerParent(username: String, email: String, password: String): ApiResult<ParentDto> {
        val result = safeApiCall { api.registerParent(RegisterRequest(username, email, password)) }
        if (result is ApiResult.Success) {
            session.saveParentToken(result.data.token)
            return ApiResult.Success(result.data.parent)
        }
        return result as ApiResult.Error
    }

    suspend fun loginParent(email: String, password: String): ApiResult<ParentDto> {
        val result = safeApiCall { api.loginParent(LoginRequest(email, password)) }
        if (result is ApiResult.Success) {
            session.saveParentToken(result.data.token)
            return ApiResult.Success(result.data.parent)
        }
        return result as ApiResult.Error
    }

    suspend fun logoutParent() {
        safeApiCall { api.logoutParent() }
        session.clearParentSession()
    }

    suspend fun loginEnfant(enfantId: Int, pinCode: String): ApiResult<Unit> {
        val result = safeApiCall { api.loginEnfant(EnfantLoginRequest(enfantId, pinCode)) }
        if (result is ApiResult.Success) {
            session.saveEnfantSession(result.data.enfantToken, result.data.enfant)
            return ApiResult.Success(Unit)
        }
        return result as ApiResult.Error
    }

    suspend fun logoutEnfant() {
        safeApiCall { api.logoutEnfant() }
        session.clearEnfantSession()
    }
}
