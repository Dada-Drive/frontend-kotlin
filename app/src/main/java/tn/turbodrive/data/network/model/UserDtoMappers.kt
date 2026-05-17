// Équivalent Swift : mapping JSON /auth/me → User domain (AuthModels.swift)
package tn.turbodrive.data.network.model

import tn.turbodrive.domain.models.User

fun UserDto.toDomainUser(profilePictureOverride: String? = null): User =
    User(
        id = id,
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phone.orEmpty(),
        role = role.ifBlank { "rider" },
        profilePictureUri = profilePictureOverride ?: avatarUrl,
    )

fun OtpUserDto.toDomainUser(phoneFallback: String = ""): User =
    User(
        id = id,
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phoneNumber = phone ?: phoneFallback,
        role = role.ifBlank { "rider" },
        profilePictureUri = avatarUrl,
    )
