package com.pocketlegal.advice.domain.repository

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    val authToken: Flow<String?>
    suspend fun login(email: String, password: String): NetworkResult<User>
    suspend fun signUp(email: String, password: String, displayName: String): NetworkResult<User>
    suspend fun logout()
}
