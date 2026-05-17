package tn.turbodrive.data.storage

interface TokenStorage {
    fun saveAccessToken(accessToken: String)

    fun saveRefreshToken(refreshToken: String)

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
    )

    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    fun clearTokens()

    fun getOrCreateDeviceId(): String

    fun saveDeviceId(deviceId: String)

    fun setGuestBrowseEnabled(enabled: Boolean)

    fun isGuestBrowseEnabled(): Boolean
}
