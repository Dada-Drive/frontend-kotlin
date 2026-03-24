package com.dadadrive.data.repository

import com.dadadrive.data.remote.api.DadaDriveApiService
import com.dadadrive.data.remote.dto.CreateFolderRequestDto
import com.dadadrive.data.remote.dto.FileDto
import com.dadadrive.data.remote.dto.RenameRequestDto
import com.dadadrive.domain.model.DriveFile
import com.dadadrive.domain.repository.FileRepository
import com.dadadrive.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val apiService: DadaDriveApiService
) : FileRepository {

    override fun getFiles(parentId: String?): Flow<Result<List<DriveFile>>> = flow {
        emit(Result.Loading)
        try {
            val files = apiService.getFiles(parentId).map { it.toDomain() }
            emit(Result.Success(files))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load files", e))
        }
    }

    override suspend fun getFileById(id: String): Result<DriveFile> =
        try {
            Result.Success(apiService.getFileById(id).toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load file", e)
        }

    override suspend fun deleteFile(id: String): Result<Unit> =
        try {
            apiService.deleteFile(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete file", e)
        }

    override suspend fun renameFile(id: String, newName: String): Result<DriveFile> =
        try {
            Result.Success(apiService.renameFile(id, RenameRequestDto(newName)).toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to rename file", e)
        }

    override suspend fun createFolder(name: String, parentId: String?): Result<DriveFile> =
        try {
            Result.Success(apiService.createFolder(CreateFolderRequestDto(name, parentId)).toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create folder", e)
        }

    private fun FileDto.toDomain() = DriveFile(
        id = id,
        name = name,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        parentId = parentId,
        isFolder = isFolder,
        downloadUrl = downloadUrl
    )
}
