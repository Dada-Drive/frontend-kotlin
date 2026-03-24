package com.dadadrive.domain.repository

import com.dadadrive.domain.model.DriveFile
import com.dadadrive.utils.Result
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    fun getFiles(parentId: String? = null): Flow<Result<List<DriveFile>>>
    suspend fun getFileById(id: String): Result<DriveFile>
    suspend fun deleteFile(id: String): Result<Unit>
    suspend fun renameFile(id: String, newName: String): Result<DriveFile>
    suspend fun createFolder(name: String, parentId: String? = null): Result<DriveFile>
}
