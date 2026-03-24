package com.dadadrive.domain.model

data class DriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long,
    val modifiedAt: Long,
    val parentId: String? = null,
    val isFolder: Boolean = false,
    val downloadUrl: String? = null
)
