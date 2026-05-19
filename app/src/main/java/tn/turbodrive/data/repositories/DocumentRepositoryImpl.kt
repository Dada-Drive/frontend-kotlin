package tn.turbodrive.data.repositories

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import tn.turbodrive.data.network.api.UploadApiService
import tn.turbodrive.data.network.dto.OcrFieldsDto
import tn.turbodrive.data.network.dto.OcrPollResponseDto
import tn.turbodrive.data.network.dto.OcrUploadResponseDto
import tn.turbodrive.data.network.envelope.unwrap
import tn.turbodrive.domain.models.OcrDocType
import tn.turbodrive.domain.models.OcrFields
import tn.turbodrive.domain.models.OcrLifecycle
import tn.turbodrive.domain.models.OcrPollResult
import tn.turbodrive.domain.models.OcrUploadedDocument
import tn.turbodrive.domain.protocols.DocumentRepository
import java.io.File
import javax.inject.Inject

class DocumentRepositoryImpl
    @Inject
    constructor(
        private val api: UploadApiService,
    ) : DocumentRepository {
        override suspend fun uploadForOcr(
            file: File,
            docType: String,
        ): Result<OcrUploadedDocument> {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val filePart =
                MultipartBody.Part.createFormData(
                    name = "document",
                    filename = file.name,
                    body = file.asRequestBody(mediaType),
                )
            val docTypePart = docType.toRequestBody("text/plain".toMediaTypeOrNull())

            return api
                .uploadForOcr(filePart, docTypePart)
                .unwrap<OcrUploadResponseDto>()
                .map { it.document.toDomain() }
        }

        override suspend fun getOcrStatus(documentId: String): Result<OcrPollResult> =
            api
                .getOcrStatus(documentId)
                .unwrap<OcrPollResponseDto>()
                .map { it.toDomain() }
    }

private fun tn.turbodrive.data.network.dto.OcrUploadedDocumentDto.toDomain(): OcrUploadedDocument =
    OcrUploadedDocument(
        id = id,
        url = url,
        docType = OcrDocType.fromWire(docType),
        status = OcrLifecycle.fromWire(status),
    )

private fun OcrPollResponseDto.toDomain(): OcrPollResult =
    OcrPollResult(
        documentId = documentId,
        docType = OcrDocType.fromWire(docType),
        status = OcrLifecycle.fromWire(status),
        fields = fields?.toDomain(),
        confidence = meta?.confidence,
        rejectionReason = rejectionReason,
    )

private fun OcrFieldsDto.toDomain(): OcrFields =
    OcrFields(
        nom = nom,
        prenom = prenom,
        numeroCin = numeroCin,
        dateNaissance = dateNaissance,
        numero = numero,
        dateExpiration = dateExpiration,
        categories = categories,
        warnings = warnings,
    )
