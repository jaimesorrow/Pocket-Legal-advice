package com.pocketlegal.advice.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.core.util.Constants
import com.pocketlegal.advice.core.util.safeApiCall
import com.pocketlegal.advice.data.model.User
import com.pocketlegal.advice.data.remote.AuthApiService
import com.pocketlegal.advice.data.remote.LoginRequest
import com.pocketlegal.advice.data.remote.SignUpRequest
import com.pocketlegal.advice.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val dataStore: DataStore<Preferences>
) : IAuthRepository {

    private val tokenKey = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)

    override val authToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[tokenKey]
    }

    override suspend fun login(email: String, password: String): NetworkResult<User> {
        val result = safeApiCall { authApiService.login(LoginRequest(email, password)) }
        if (result is NetworkResult.Success) {
            saveToken(result.data.token)
        }
        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.user)
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): NetworkResult<User> {
        val result = safeApiCall {
            authApiService.signUp(SignUpRequest(email, password, displayName))
        }
        if (result is NetworkResult.Success) {
            saveToken(result.data.token)
        }
        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.user)
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    override suspend fun logout() {
        dataStore.edit { prefs -> prefs.remove(tokenKey) }
    }

    private suspend fun saveToken(token: String) {
        dataStore.edit { prefs -> prefs[tokenKey] = token }
    }
}
