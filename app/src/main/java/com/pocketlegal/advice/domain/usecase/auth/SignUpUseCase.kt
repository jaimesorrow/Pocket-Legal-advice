package com.pocketlegal.advice.domain.usecase.auth

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.User
import com.pocketlegal.advice.domain.repository.IAuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): NetworkResult<User> = authRepository.signUp(email, password, displayName)
}
