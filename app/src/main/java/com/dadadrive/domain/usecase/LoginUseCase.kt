package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.UserRepository
import com.dadadrive.utils.Result
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        userRepository.login(email, password)
}
