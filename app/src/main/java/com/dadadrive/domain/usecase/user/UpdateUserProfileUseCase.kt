package com.dadadrive.domain.usecase.user

import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.UserRepository
import com.dadadrive.utils.Resource
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Resource<User> {
        if (user.fullName.isBlank()) return Resource.Error("Le nom complet est requis")
        if (!user.email.contains("@")) return Resource.Error("Adresse email invalide")
        if (user.phone.isBlank()) return Resource.Error("Le numéro de téléphone est requis")
        return userRepository.updateUserProfile(user)
    }
}
