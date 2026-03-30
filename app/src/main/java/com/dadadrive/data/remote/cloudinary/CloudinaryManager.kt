package com.dadadrive.data.remote.cloudinary

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère les opérations Cloudinary pour les photos de profil.
 *
 * Stratégie overwrite : même publicId par utilisateur (user_<id>)
 * → chaque upload remplace l'image précédente sans suppression séparée.
 *
 * Upload local (galerie) : OkHttp multipart (évite le SDK qui exige api_key pour les fichiers larges)
 * Upload URL distante (Google photo) : Cloudinary REST API via OkHttp (fetch remote URL)
 */
@Singleton
class CloudinaryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CloudinaryManager"
        private const val CLOUD_NAME = "dzmbdnhh4"
        private const val UPLOAD_PRESET = "dadadrive_avatars"
        private const val UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // ─────────────────────────────────────────────────────────────────────────
    // Upload depuis la galerie (URI local) — OkHttp multipart
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Upload une image locale vers Cloudinary via multipart HTTP.
     *
     * N'utilise PAS le SDK MediaManager pour éviter l'erreur "Must supply api_key"
     * déclenchée quand le SDK bascule sur uploadLarge (fichiers > seuil).
     *
     * @param uri URI du fichier (content:// ou file://)
     * @param publicId ID public Cloudinary. Overwrite si déjà existant.
     * @return secure_url de l'image hébergée sur Cloudinary
     */
    suspend fun uploadImage(uri: Uri, publicId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("Impossible d'ouvrir l'image"))

                val imageBytes = inputStream.use { it.readBytes() }
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        name = "file",
                        filename = "photo.jpg",
                        body = imageBytes.toRequestBody(mimeType.toMediaType())
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("public_id", publicId)
                    // overwrite interdit avec unsigned upload — activé dans les settings du preset
                    .build()

                val request = Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = runCatching {
                        JSONObject(responseBody).getJSONObject("error").getString("message")
                    }.getOrDefault("HTTP ${response.code}")
                    Log.e(TAG, "uploadImage ERREUR HTTP ${response.code}: $responseBody")
                    return@withContext Result.failure(Exception(errorMsg))
                }
                Log.d(TAG, "uploadImage HTTP ${response.code} OK")

                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")

                if (secureUrl.isNotBlank()) {
                    Log.i(TAG, "Upload local réussi : $secureUrl")
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("secure_url absent dans la réponse Cloudinary"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception uploadImage", e)
                Result.failure(e)
            }
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Upload depuis une URL distante (photo Google)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Upload une image distante (ex: Google profile photo URL) vers Cloudinary
     * via la REST API (Cloudinary fetche l'image depuis l'URL).
     *
     * @param remoteUrl URL HTTPS de l'image source
     * @param publicId  ID public Cloudinary
     * @return secure_url de l'image hébergée sur Cloudinary
     */
    suspend fun uploadFromUrl(remoteUrl: String, publicId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val body = FormBody.Builder()
                    .add("file", remoteUrl)
                    .add("upload_preset", UPLOAD_PRESET)
                    .add("public_id", publicId)
                    // overwrite interdit avec unsigned upload — activé dans les settings du preset
                    .build()

                val request = Request.Builder()
                    .url(UPLOAD_URL)
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = runCatching {
                        JSONObject(responseBody).getJSONObject("error").getString("message")
                    }.getOrDefault("HTTP ${response.code}")
                    Log.e(TAG, "uploadFromUrl ERREUR HTTP ${response.code}: $responseBody")
                    return@withContext Result.failure(Exception(errorMsg))
                }
                Log.d(TAG, "uploadFromUrl HTTP ${response.code} OK")

                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")

                if (secureUrl.isNotBlank()) {
                    Log.i(TAG, "Upload URL réussi : $secureUrl")
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("secure_url absent dans la réponse Cloudinary"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception uploadFromUrl", e)
                Result.failure(e)
            }
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Suppression — stratégie overwrite (pas d'API secret côté client)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Suppression via Admin API nécessite l'API secret → non exposé côté client.
     * La stratégie publicId fixe (user_<id>) + overwrite=true remplace l'image
     * à chaque upload sans suppression explicite.
     *
     * Pour une vraie suppression : appeler le backend qui possède les credentials.
     */
    fun deleteImage(publicId: String): Result<Unit> {
        Log.d(TAG, "deleteImage ignoré — stratégie overwrite pour publicId: $publicId")
        return Result.success(Unit)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaire
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extrait le publicId depuis une URL Cloudinary.
     *
     * Exemple :
     *   https://res.cloudinary.com/dzmbdnhh4/image/upload/v1234/dadadrive_avatars/user_abc.jpg
     *   → dadadrive_avatars/user_abc
     */
    fun extractPublicId(cloudinaryUrl: String): String {
        return try {
            val parts = cloudinaryUrl.split("/")
            val uploadIndex = parts.indexOf("upload")
            if (uploadIndex < 0 || uploadIndex + 1 >= parts.size) return ""

            // Sauter le numéro de version (v1234567890)
            val afterUpload = parts.drop(uploadIndex + 1)
            val relevant = if (afterUpload.firstOrNull()?.matches(Regex("v\\d+")) == true) {
                afterUpload.drop(1)
            } else afterUpload

            relevant.joinToString("/").substringBeforeLast(".")
        } catch (e: Exception) {
            Log.e(TAG, "extractPublicId failed pour: $cloudinaryUrl", e)
            ""
        }
    }

    /** Génère un publicId déterministe pour un utilisateur (sans dossier — requis par les presets unsigned). */
    fun publicIdForUser(userId: String): String = "user_$userId"
}
