package tn.dadadrive.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.dadadrive.data.local.entity.CachedUserProfileEntity
import tn.dadadrive.domain.models.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionProfileCache @Inject constructor(
    private val appDatabase: AppDatabase,
) {

    suspend fun save(user: User) = withContext(Dispatchers.IO) {
        appDatabase.cachedUserProfileDao().upsert(user.toEntity())
    }

    suspend fun loadUser(): User? = withContext(Dispatchers.IO) {
        appDatabase.cachedUserProfileDao().getLatest()?.toUser()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        appDatabase.cachedUserProfileDao().clearAll()
    }

    private fun User.toEntity(): CachedUserProfileEntity =
        CachedUserProfileEntity(
            id = id,
            fullName = fullName.ifBlank { null },
            phone = phoneNumber.ifBlank { null },
            email = email.ifBlank { null },
            avatarUrl = profilePictureUri,
            role = role.ifBlank { null },
            cachedAt = System.currentTimeMillis(),
        )

    private fun CachedUserProfileEntity.toUser(): User =
        User(
            id = id,
            fullName = fullName.orEmpty(),
            email = email.orEmpty(),
            phoneNumber = phone.orEmpty(),
            role = role.orEmpty().ifBlank { "rider" },
            profilePictureUri = avatarUrl,
        )
}
