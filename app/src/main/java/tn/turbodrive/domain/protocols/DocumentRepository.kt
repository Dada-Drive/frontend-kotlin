package tn.turbodrive.domain.protocols

import tn.turbodrive.domain.models.OcrPollResult
import tn.turbodrive.domain.models.OcrUploadedDocument
import java.io.File

/**
 * R-5.2 — Domain-level OCR repository.
 *
 * Wraps the dual-shape backend endpoints :
 *  - [uploadForOcr] : POST multipart, returns a freshly queued document
 *  - [getOcrStatus] : GET polling, returns the filtered OCR view
 *
 * Returns [Result] so call sites can `.fold(onSuccess, onFailure)` and
 * forward [tn.turbodrive.data.network.envelope.BackendException] errors
 * through the standard error envelope path.
 */
interface DocumentRepository {
    suspend fun uploadForOcr(
        file: File,
        docType: String,
    ): Result<OcrUploadedDocument>

    suspend fun getOcrStatus(documentId: String): Result<OcrPollResult>
}
