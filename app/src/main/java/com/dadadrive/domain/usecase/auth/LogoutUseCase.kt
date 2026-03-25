package com.dadadrive.domain.usecase.auth

import com.dadadrive.domain.repository.AuthRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Unit> = authRepository.logout()
}
