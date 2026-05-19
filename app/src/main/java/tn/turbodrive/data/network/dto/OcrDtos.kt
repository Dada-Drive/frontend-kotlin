package tn.turbodrive.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * R-5.2 — DTOs for the dual-shape OCR endpoints.
 *
 * Backend (dada-api/src/controllers/ocrController.ts) exposes two endpoints with
 * **different response shapes** :
 *
 *  - POST /upload/document/ocr → [OcrUploadResponseDto]
 *      { document: { id, url, key, docType, status: 'queued', adminStatus } }
 *
 *  - GET /upload/document/ocr/:id → [OcrPollResponseDto]
 *      { documentId, docType, status: 'ok'|'review'|'failed'|null, adminStatus,
 *        fields: OcrFieldsDto?, meta: OcrMetaDto?, rejectionReason }
 *
 * The shapes diverge because POST returns the freshly persisted upload
 * envelope (controller line 86-95) while GET returns the OwnDocumentView
 * (services/ocrService.ts line 120-128) optimised for driver-side polling.
 */

data class OcrUploadResponseDto(
    @SerializedName("document")
    val document: OcrUploadedDocumentDto,
)

data class OcrUploadedDocumentDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("key")
    val key: String? = null,
    @SerializedName("docType")
    val docType: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("adminStatus")
    val adminStatus: String? = null,
)

data class OcrPollResponseDto(
    @SerializedName("documentId")
    val documentId: String,
    @SerializedName("docType")
    val docType: String,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("adminStatus")
    val adminStatus: String? = null,
    @SerializedName("fields")
    val fields: OcrFieldsDto? = null,
    @SerializedName("meta")
    val meta: OcrMetaDto? = null,
    @SerializedName("rejectionReason")
    val rejectionReason: String? = null,
)

/**
 * Filtered Option A fields. Only the keys relevant to the polled doc type are populated ;
 * the others stay null. Polymorphic by [OcrPollResponseDto.docType].
 *
 *  - CIN     : nom, prenom, numero_cin, date_naissance
 *  - Permis  : nom, prenom, numero, date_expiration, categories, warnings
 *  - Others  : currently V1 ignores carte_grise / assurance (deferred)
 */
data class OcrFieldsDto(
    @SerializedName("nom")
    val nom: String? = null,
    @SerializedName("prenom")
    val prenom: String? = null,
    @SerializedName("numero_cin")
    val numeroCin: String? = null,
    @SerializedName("date_naissance")
    val dateNaissance: String? = null,
    @SerializedName("numero")
    val numero: String? = null,
    @SerializedName("date_expiration")
    val dateExpiration: String? = null,
    @SerializedName("categories")
    val categories: List<String>? = null,
    @SerializedName("warnings")
    val warnings: List<String>? = null,
)

data class OcrMetaDto(
    @SerializedName("confidence")
    val confidence: Double? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("processing_time_ms")
    val processingTimeMs: Long? = null,
)
