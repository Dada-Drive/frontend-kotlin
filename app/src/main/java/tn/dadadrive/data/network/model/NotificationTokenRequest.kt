package tn.dadadrive.data.network.model

data class NotificationTokenRequest(
    val token: String,
    val platform: String = "android",
)
