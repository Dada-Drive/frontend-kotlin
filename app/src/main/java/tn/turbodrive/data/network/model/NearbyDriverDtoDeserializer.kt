package tn.turbodrive.data.network.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Postgres / Express may emit numbers as JSON numbers or (rarely) strings; [id] can be UUID
 * string or number. Gson's default bean mapping fails on type mismatch and drops the whole
 * nearby-drivers response — riders then see no taxi markers.
 */
internal class NearbyDriverDtoDeserializer : JsonDeserializer<NearbyDriverDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): NearbyDriverDto {
        val o =
            json?.takeIf { it.isJsonObject }?.asJsonObject
                ?: JsonObject()
        return NearbyDriverDto(
            id = o.flexString("id"),
            fullName = o.flexStringOrNull("full_name"),
            lastLat = o.flexDouble("last_lat"),
            lastLng = o.flexDouble("last_lng"),
            lastHeading = o.flexDoubleOrNull("last_heading"),
            distanceKm = o.flexDouble("distance_km"),
        )
    }

    private fun JsonObject.flexString(key: String): String {
        val e = get(key) ?: return ""
        if (e.isJsonNull) return ""
        if (!e.isJsonPrimitive) return e.toString().trim('"')
        val p = e.asJsonPrimitive
        return when {
            p.isString -> p.asString
            p.isNumber -> p.asNumber.toString()
            p.isBoolean -> p.asBoolean.toString()
            else -> ""
        }
    }

    private fun JsonObject.flexStringOrNull(key: String): String? {
        val s = flexString(key)
        return s.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.flexDouble(key: String): Double {
        val e = get(key) ?: return 0.0
        if (e.isJsonNull) return 0.0
        if (!e.isJsonPrimitive) return 0.0
        val p = e.asJsonPrimitive
        return when {
            p.isNumber -> p.asDouble
            p.isString -> p.asString.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun JsonObject.flexDoubleOrNull(key: String): Double? {
        val e = get(key) ?: return null
        if (e.isJsonNull) return null
        if (!e.isJsonPrimitive) return null
        val p = e.asJsonPrimitive
        val v =
            when {
                p.isNumber -> p.asDouble
                p.isString -> p.asString.toDoubleOrNull()
                else -> null
            }
        return v
    }
}
