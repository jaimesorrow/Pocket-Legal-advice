package com.pocketlegal.advice.domain.usecase.auth

import com.pocketlegal.advice.core.network.NetworkResult
import com.pocketlegal.advice.data.model.User
import com.pocketlegal.advice.domain.repository.IAuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(email: String, password: String): NetworkResult<User> =
        authRepository.login(email, password)
}
