package tn.dadadrive.core.logging

enum class RedactionType {
    TOKEN,
    PHONE,
    FULL_NAME,
    EMAIL,
    COORDS,
    AMOUNT,
    FCM_TOKEN,
    URL_QUERY,
    MEDIA_URL,
    OTP,
    DEVICE_ID,
    GENERIC,
}

object LogRedaction {

    fun redact(value: String, type: RedactionType): String = when (type) {
        RedactionType.TOKEN -> "[TOKEN]"
        RedactionType.EMAIL -> "[EMAIL]"
        RedactionType.COORDS -> "[COORDS]"
        RedactionType.AMOUNT -> "[AMOUNT]"
        RedactionType.FCM_TOKEN -> "[FCM_TOKEN]"
        RedactionType.MEDIA_URL -> "[MEDIA_URL]"
        RedactionType.OTP -> "[OTP]"
        RedactionType.PHONE -> redactPhone(value)
        RedactionType.FULL_NAME -> redactFullName(value)
        RedactionType.DEVICE_ID -> redactDeviceId(value)
        RedactionType.URL_QUERY -> stripQuery(value)
        RedactionType.GENERIC -> if (value.length > 120) value.take(120) + "…" else value
    }

    fun redactHttpHeader(name: String, value: String?): String? {
        if (value.isNullOrBlank()) return value
        val lower = name.lowercase()
        return when {
            lower == "authorization" -> "[TOKEN]"
            lower.contains("token") -> "[TOKEN]"
            lower == "cookie" -> "[COOKIE]"
            lower == "idempotency-key" || lower == "x-idempotency-key" -> redactDeviceId(value)
            else -> value
        }
    }

    fun redactRequestBodyForLog(body: String): String {
        var s = body
        s = Regex("(\"refreshToken\"\\s*:\\s*\")([^\"]+)(\")").replace(s) { "${it.groupValues[1]}[TOKEN]${it.groupValues[3]}" }
        s = Regex("(\"accessToken\"\\s*:\\s*\")([^\"]+)(\")").replace(s) { "${it.groupValues[1]}[TOKEN]${it.groupValues[3]}" }
        s = Regex("(\"token\"\\s*:\\s*\")([^\"]+)(\")", RegexOption.IGNORE_CASE).replace(s) { "${it.groupValues[1]}[TOKEN]${it.groupValues[3]}" }
        return s
    }

    private fun redactPhone(value: String): String {
        val digits = value.filter { it.isDigit() }
        if (digits.length < 4) return "***"
        return "***${digits.takeLast(4)}"
    }

    private fun redactFullName(value: String): String {
        val parts = value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (parts.isEmpty()) return "[NAME]"
        if (parts.size == 1) return "${parts[0].take(1)}."
        return "${parts[0]} ${parts.last().take(1)}."
    }

    private fun redactDeviceId(value: String): String =
        if (value.length <= 8) "***" else "${value.take(8)}…"

    private fun stripQuery(url: String): String {
        val q = url.indexOf('?')
        return if (q >= 0) url.substring(0, q) else url
    }
}
