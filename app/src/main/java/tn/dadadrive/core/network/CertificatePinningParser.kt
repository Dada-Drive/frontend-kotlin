package tn.dadadrive.core.network

/**
 * Parses the CERTIFICATE_PINS BuildConfig string into a flat list of (host, pin) pairs.
 *
 * Format: comma-separated entries, each entry pipe-separated as
 * `host|sha256/A=,host|sha256/B=...`. Multiple pins for the same host may share an
 * entry (`host|sha256/A=|sha256/B=`) or appear as separate entries. Empty / blank
 * input returns an empty list. Entries without at least one pin are skipped.
 *
 * The parser does NOT validate pin format (e.g. presence of `sha256/` prefix or
 * base64 length) — that validation is delegated to `okhttp3.CertificatePinner.Builder.add`,
 * which throws on malformed input at construction time.
 */
internal fun parseCertificatePins(raw: String): List<Pair<String, String>> {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return emptyList()
    return buildList {
        for (entry in trimmed.split(',').map { it.trim() }.filter { it.isNotEmpty() }) {
            val parts = entry.split('|').map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.size < 2) continue
            val host = parts[0]
            for (pin in parts.drop(1)) {
                add(host to pin)
            }
        }
    }
}
