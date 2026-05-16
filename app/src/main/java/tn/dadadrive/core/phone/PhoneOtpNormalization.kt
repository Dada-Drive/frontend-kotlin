package tn.dadadrive.core.phone

/**
 * Aligns with `otpService.sendOtp` / `verifyOtp`: strip `+` / `00`, keep digits only.
 * Stored in DB and used for Vonage `to` as this exact digit string (no `+`).
 */
fun normalizePhoneForOtpBackend(raw: String): String {
    var s = raw.trim()
    if (s.startsWith("00")) s = s.drop(2)
    if (s.startsWith("+")) s = s.drop(1)
    return s.filter { it.isDigit() }
}
