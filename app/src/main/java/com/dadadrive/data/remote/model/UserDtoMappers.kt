// Équivalent Swift : mapping JSON /auth/me → User domain (AuthModels.swift)
package com.dadadrive.data.remote.model

import com.dadadrive.domain.model.User

fun UserDto.toDomainUser(profilePictureOverride: String? = null): User =
    User(
        id = id,
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phone.orEmpty(),
        role = role.ifBlank { "rider" },
        profilePictureUri = profilePictureOverride ?: avatarUrl
    )
