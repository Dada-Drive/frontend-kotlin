package tn.turbodrive.domain.usecases.driver

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tn.turbodrive.domain.models.OcrLifecycle
import tn.turbodrive.domain.models.OcrPollResult
import tn.turbodrive.domain.models.OcrUploadedDocument
import tn.turbodrive.domain.protocols.DocumentRepository
import java.io.File
import javax.inject.Inject

/**
 * R-5.2 — Upload a document for OCR and poll for the result.
 *
 * Workflow :
 *  1. Upload multipart (document + docType)
 *  2. Backend persists immediately and returns `{ id, status: 'queued' }`
 *  3. Poll GET /upload/document/ocr/:id every [POLL_INTERVAL_MS]
 *  4. Stop when status = Ready (success) or Failed
 *  5. Time out after [POLL_MAX_ATTEMPTS] x [POLL_INTERVAL_MS] (60 s default)
 *
 * Emits [OcrProgress] states as a cold Flow so call sites can drive a
 * loading UI directly off `.collect { }`.
 */
class UploadAndPollOcrUseCase
    @Inject
    constructor(
        private val documentRepository: DocumentRepository,
    ) {
        operator fun invoke(
            file: File,
            docType: String,
        ): Flow<OcrProgress> =
            flow {
                emit(OcrProgress.Uploading)

                val uploaded =
                    documentRepository
                        .uploadForOcr(file, docType)
                        .fold(
                            onSuccess = { it },
                            onFailure = {
                                emit(OcrProgress.UploadFailed(it.message ?: "Upload failed"))
                                return@flow
                            },
                        )

                emit(OcrProgress.Processing(uploaded))

                repeat(POLL_MAX_ATTEMPTS) {
                    delay(POLL_INTERVAL_MS)

                    val poll =
                        documentRepository
                            .getOcrStatus(uploaded.id)
                            .fold(
                                onSuccess = { it },
                                onFailure = {
                                    emit(OcrProgress.PollFailed(it.message ?: "Poll failed"))
                                    return@flow
                                },
                            )

                    when (poll.status) {
                        OcrLifecycle.Ready -> {
                            emit(OcrProgress.Ready(poll))
                            return@flow
                        }
                        OcrLifecycle.Failed -> {
                            emit(OcrProgress.OcrFailed(poll.rejectionReason ?: "OCR failed"))
                            return@flow
                        }
                        OcrLifecycle.Queued, OcrLifecycle.Processing -> {
                            // Continue polling — emit progress so UI can refresh hints.
                            emit(OcrProgress.Processing(uploaded))
                        }
                    }
                }

                emit(OcrProgress.Timeout)
            }

        private companion object {
            private const val POLL_INTERVAL_MS = 2_000L
            private const val POLL_MAX_ATTEMPTS = 30
        }
    }

sealed class OcrProgress {
    data object Uploading : OcrProgress()

    data class UploadFailed(val message: String) : OcrProgress()

    data class Processing(val uploaded: OcrUploadedDocument) : OcrProgress()

    data class Ready(val result: OcrPollResult) : OcrProgress()

    data class OcrFailed(val message: String) : OcrProgress()

    data class PollFailed(val message: String) : OcrProgress()

    data object Timeout : OcrProgress()
}
