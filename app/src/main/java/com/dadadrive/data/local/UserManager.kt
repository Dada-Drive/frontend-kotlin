package com.dadadrive.data.local

import android.content.Context
import android.content.SharedPreferences
import com.dadadrive.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dadadrive_user_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("full_name", user.fullName)
            .putString("email", user.email)
            .putString("phone_number", user.phoneNumber)
            .putString("role", user.role)
            .putString("avatar_url", user.profilePictureUri)
            .apply()
    }

    fun getUser(): User? {
        val id = prefs.getString("user_id", null) ?: return null
        return User(
            id = id,
            fullName = prefs.getString("full_name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            phoneNumber = prefs.getString("phone_number", "") ?: "",
            role = prefs.getString("role", "rider") ?: "rider",
            profilePictureUri = prefs.getString("avatar_url", null)
        )
    }

    fun updateFullName(name: String) {
        prefs.edit().putString("full_name", name).apply()
    }

    fun updateAvatarUri(uri: String) {
        prefs.edit().putString("avatar_url", uri).apply()
    }

    /** Sauvegarde le publicId Cloudinary pour overwrite lors du prochain upload. */
    fun saveCloudinaryPublicId(publicId: String) {
        prefs.edit().putString("cloudinary_public_id", publicId).apply()
    }

    fun getCloudinaryPublicId(): String? =
        prefs.getString("cloudinary_public_id", null)

    fun updateRole(role: String) {
        prefs.edit().putString("role", role).apply()
    }

    fun clearUser() {
        prefs.edit().clear().apply()
    }
}
