package com.dadadrive.domain.usecase

import com.dadadrive.domain.model.DriveFile
import com.dadadrive.domain.repository.FileRepository
import com.dadadrive.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(parentId: String? = null): Flow<Result<List<DriveFile>>> =
        fileRepository.getFiles(parentId)
}
