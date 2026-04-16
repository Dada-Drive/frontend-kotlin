package com.dadadrive.data.remote

import com.google.gson.JsonParser
import retrofit2.HttpException

object ApiErrorParser {

    fun httpMessage(e: HttpException): String {
        val fallback = e.message()?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()}"
        val body = try {
            e.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        } ?: return fallback
        return try {
            val el = JsonParser.parseString(body)
            if (el.isJsonObject) {
                val msg = el.asJsonObject.get("message")?.asString
                if (!msg.isNullOrBlank()) return msg
            }
            fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
