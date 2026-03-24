package com.dadadrive.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String?,
    val storageUsedBytes: Long,
    val storageLimitBytes: Long
)

data class FileDto(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val parentId: String?,
    val isFolder: Boolean,
    val downloadUrl: String?
)

data class RenameRequestDto(val name: String)

data class CreateFolderRequestDto(val name: String, val parentId: String? = null)
