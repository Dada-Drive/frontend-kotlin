package tn.turbodrive.data.network.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import tn.turbodrive.data.network.annotation.Idempotent
import tn.turbodrive.data.network.dto.OcrPollResponseDto
import tn.turbodrive.data.network.dto.OcrUploadResponseDto
import tn.turbodrive.data.network.envelope.ApiResponse

/**
 * R-5.2 — OCR upload + polling endpoints (dada-api `uploadRoutes`).
 *
 * Backend multipart field name is `document` (cf. ocrController.ts line 26),
 * **not** `file` — getting this wrong returns a 400 VALIDATION_ERROR.
 */
interface UploadApiService {
    @Multipart
    @Idempotent
    @POST("upload/document/ocr")
    suspend fun uploadForOcr(
        @Part document: MultipartBody.Part,
        @Part("docType") docType: RequestBody,
    ): Response<ApiResponse<OcrUploadResponseDto>>

    @GET("upload/document/ocr/{documentId}")
    suspend fun getOcrStatus(
        @Path("documentId") documentId: String,
    ): Response<ApiResponse<OcrPollResponseDto>>
}
