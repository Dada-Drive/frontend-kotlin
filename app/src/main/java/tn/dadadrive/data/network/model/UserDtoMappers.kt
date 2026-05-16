// Équivalent Swift : mapping JSON /auth/me → User domain (AuthModels.swift)
package tn.dadadrive.data.network.model

import tn.dadadrive.domain.models.User

fun UserDto.toDomainUser(profilePictureOverride: String? = null): User =
    User(
        id = id,
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phone.orEmpty(),
        role = role.ifBlank { "rider" },
        profilePictureUri = profilePictureOverride ?: avatarUrl
    )

fun OtpUserDto.toDomainUser(phoneFallback: String = ""): User =
    User(
        id = id,
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phone ?: phoneFallback,
        role = role.ifBlank { "rider" },
        profilePictureUri = avatarUrl
    )
