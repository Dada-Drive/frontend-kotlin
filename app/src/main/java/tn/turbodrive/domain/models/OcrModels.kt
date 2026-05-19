package tn.turbodrive.domain.models

/**
 * R-5.2 — Domain OCR models.
 *
 * Insulates presentation from the wire DTOs. The polling endpoint serves
 * a tri-state lifecycle (queued → ok|review|failed) and Option A field
 * filters that are flattened into typed properties here.
 */

data class OcrUploadedDocument(
    val id: String,
    val url: String?,
    val docType: OcrDocType,
    val status: OcrLifecycle,
)

data class OcrPollResult(
    val documentId: String,
    val docType: OcrDocType,
    val status: OcrLifecycle,
    val fields: OcrFields?,
    val confidence: Double?,
    val rejectionReason: String?,
)

enum class OcrDocType(val wire: String) {
    Cin("cin"),
    Permis("permis"),
    CarteGrise("carte_grise"),
    Assurance("assurance"),
    ;

    companion object {
        fun fromWire(value: String?): OcrDocType = entries.firstOrNull { it.wire == value } ?: Cin
    }
}

/**
 * Unified lifecycle across upload+poll.
 *
 *  - Queued     : POST returned `status=queued`, or GET returned `status=null`
 *  - Processing : reserved (backend currently flips queued → ok|review|failed)
 *  - Ready      : GET `status=ok|review` and `fields` populated
 *  - Failed     : GET `status=failed`
 */
enum class OcrLifecycle {
    Queued,
    Processing,
    Ready,
    Failed,
    ;

    companion object {
        fun fromWire(value: String?): OcrLifecycle =
            when (value) {
                "queued" -> Queued
                "processing" -> Processing
                "ok", "review" -> Ready
                "failed" -> Failed
                null -> Queued
                else -> Queued
            }
    }
}

data class OcrFields(
    val nom: String? = null,
    val prenom: String? = null,
    val numeroCin: String? = null,
    val dateNaissance: String? = null,
    val numero: String? = null,
    val dateExpiration: String? = null,
    val categories: List<String>? = null,
    val warnings: List<String>? = null,
)
