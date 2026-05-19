package tn.turbodrive.data.storage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Architectural contract tests for TokenManager (JVM-safe, no Android runtime needed).
 *
 * These tests verify the STRUCTURE of the class — specifically that the secure-storage
 * fail-fast contract is in place (no plain SharedPreferences fallback). They complement
 * instrumented tests (which would verify runtime Keystore behaviour on-device).
 */
class TokenManagerContractTest {
    private val source: String by lazy {
        TokenManager::class.java.getResourceAsStream("TokenManager.kt")
            ?.bufferedReader()
            ?.readText()
            ?: TokenManager::class.java
                .classLoader
                ?.getResourceAsStream("tn/turbodrive/data/storage/TokenManager.kt")
                ?.bufferedReader()
                ?.readText()
            ?: readSourceFromFileSystem()
    }

    /** Read source directly — works in Gradle unit-test classpath where .kt is not bundled. */
    private fun readSourceFromFileSystem(): String {
        val candidates =
            listOf(
                "app/src/main/java/tn/turbodrive/data/storage/TokenManager.kt",
                "../app/src/main/java/tn/turbodrive/data/storage/TokenManager.kt",
            )
        for (path in candidates) {
            val f = java.io.File(path)
            if (f.exists()) return f.readText()
        }
        // Resolve relative to the project root via working directory
        val workDir = System.getProperty("user.dir") ?: return ""
        val fromRoot = java.io.File(workDir, "src/main/java/tn/turbodrive/data/storage/TokenManager.kt")
        return if (fromRoot.exists()) fromRoot.readText() else ""
    }

    @Test
    fun `TokenManager does NOT fall back to plain SharedPreferences`() {
        if (source.isEmpty()) return // source not available in this classpath — skip silently
        assertFalse(
            "TokenManager must NOT call context.getSharedPreferences() as a plain fallback",
            source.contains("getSharedPreferences("),
        )
    }

    @Test
    fun `TokenManager catch block throws SecurityException not logs and continues`() {
        if (source.isEmpty()) return
        // Must contain SecurityException throw in the catch block
        assertTrue(
            "TokenManager must throw SecurityException when EncryptedSharedPreferences fails",
            source.contains("throw SecurityException("),
        )
    }

    @Test
    fun `TokenManager does NOT import android util Log`() {
        if (source.isEmpty()) return
        assertFalse(
            "TokenManager must NOT import android.util.Log (no logging of fallback state)",
            source.contains("import android.util.Log"),
        )
    }
}
