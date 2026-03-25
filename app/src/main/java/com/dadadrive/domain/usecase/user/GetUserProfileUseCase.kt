package com.dadadrive.domain.usecase.user

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.UserRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Resource<User> {
        if (userId.isBlank()) return Resource.Error("Identifiant utilisateur invalide")
        return userRepository.getUserProfile(userId)
    }
}
