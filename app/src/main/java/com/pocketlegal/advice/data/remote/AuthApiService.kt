package com.pocketlegal.advice.data.remote

import com.pocketlegal.advice.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class SignUpRequest(val email: String, val password: String, val displayName: String)
data class AuthResponse(val token: String, val user: User)

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>
}
