package tn.turbodrive.presentation.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.search.ResponseDetails
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchError
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.SuggestCallbackExtended
import com.here.sdk.search.Suggestion
import com.here.sdk.search.TextQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Retire un préfixe Plus Code (Open Location Code) souvent placé en tête par les géocodeurs,
 * par ex. « V5P6+4FF, Ariana, Tunisie » → « Ariana, Tunisie ».
 */
internal fun formatAddressForDisplay(raw: String): String {
    val t = raw.trim()
    if (t.isEmpty()) return t
    val olcPrefix =
        Regex(
            "^[23456789CFGHJMPQRVWX]{2,8}\\+[23456789CFGHJMPQRVWX]{2,7}\\s*,?\\s*",
            RegexOption.IGNORE_CASE,
        )
    var s = t
    repeat(3) {
        val next = s.replaceFirst(olcPrefix, "").trimStart()
        if (next == s || next.isEmpty()) return@repeat
        s = next
    }
    return s.ifBlank { t }
}

/**
 * Forward (HERE suggest + Android Geocoder) and reverse geocoding used by the map route sheet.
 */
internal class MapGeocodingHelper(
    private val context: Context,
    private val searchEngine: SearchEngine?,
    private val getBiasAnchor: () -> GeoCoordinates?,
) {
    /**
     * LRU cache of recent forward-geocode queries → results.
     * Keeps typing snappy when the user back-spaces over an already-searched prefix.
     * Access must be synchronized because we may read/write from different coroutines.
     */
    private val forwardCache: LinkedHashMap<String, List<AddressSearchHit>> =
        object : LinkedHashMap<String, List<AddressSearchHit>>(
            MapViewModelConstants.ADDRESS_SEARCH_CACHE_SIZE,
            0.75f,
            true,
        ) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<AddressSearchHit>>): Boolean =
                size > MapViewModelConstants.ADDRESS_SEARCH_CACHE_SIZE
        }

    private fun cacheKey(query: String): String = query.trim().lowercase(Locale.ROOT)

    fun cachedForwardGeocode(query: String): List<AddressSearchHit>? {
        val key = cacheKey(query)
        if (key.isEmpty()) return null
        return synchronized(forwardCache) { forwardCache[key] }
    }

    private fun rememberForwardGeocode(
        query: String,
        hits: List<AddressSearchHit>,
    ) {
        val key = cacheKey(query)
        if (key.isEmpty()) return
        synchronized(forwardCache) { forwardCache[key] = hits }
    }

    suspend fun reverseGeocodeLine(
        latitude: Double,
        longitude: Double,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val line = addresses.firstOrNull()?.toReadableString()
                            if (cont.isActive) {
                                cont.resume(line)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()
                        ?.toReadableString()
                }
            } catch (_: Exception) {
                null
            }
        }

    suspend fun forwardGeocode(query: String): List<AddressSearchHit> {
        cachedForwardGeocode(query)?.let { return it }
        return withContext(Dispatchers.IO) {
            val here = forwardGeocodeHereSuggest(query)
            val hits = if (here.isNotEmpty()) here else forwardGeocodeAndroid(query)
            rememberForwardGeocode(query, hits)
            hits
        }
    }

    private suspend fun forwardGeocodeHereSuggest(query: String): List<AddressSearchHit> {
        val engine = searchEngine ?: return emptyList()
        val anchor =
            getBiasAnchor()
                ?: GeoCoordinates(
                    MapViewModelConstants.DEFAULT_SEARCH_BIAS_LAT,
                    MapViewModelConstants.DEFAULT_SEARCH_BIAS_LNG,
                )
        return suspendCancellableCoroutine { cont ->
            val textQuery = TextQuery(query, TextQuery.Area(anchor))
            val options =
                SearchOptions().apply {
                    languageCode = searchLanguageCode()
                    maxItems = 10
                }
            engine.suggest(
                textQuery,
                options,
                object : SuggestCallbackExtended {
                    override fun onSuggestExtendedCompleted(
                        searchError: SearchError?,
                        suggestions: MutableList<Suggestion>?,
                        responseDetails: ResponseDetails?,
                    ) {
                        if (!cont.isActive) return
                        if (searchError != null) {
                            cont.resume(emptyList())
                            return
                        }
                        val hits =
                            suggestions.orEmpty().mapNotNull { s ->
                                val place = s.place ?: return@mapNotNull null
                                val geo = place.geoCoordinates ?: return@mapNotNull null
                                val addrText = place.address?.addressText?.trim().orEmpty()
                                val label =
                                    addrText.takeIf { it.isNotBlank() }
                                        ?: s.title.takeIf { !it.isNullOrBlank() } ?: return@mapNotNull null
                                AddressSearchHit(label = formatAddressForDisplay(label), coordinates = geo)
                            }.distinctBy { "${it.coordinates.latitude},${it.coordinates.longitude}|${it.label}" }
                        cont.resume(hits)
                    }
                },
            )
        }
    }

    private fun searchLanguageCode(): LanguageCode =
        when (Locale.getDefault().language) {
            "fr" -> LanguageCode.FR_FR
            "ar" -> LanguageCode.AR_SA
            else -> LanguageCode.EN_US
        }

    private suspend fun forwardGeocodeAndroid(query: String): List<AddressSearchHit> {
        if (!Geocoder.isPresent()) return emptyList()
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val raw: List<Address> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocationName(query, 5) { addresses ->
                            if (cont.isActive) cont.resume(addresses ?: emptyList())
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 5) ?: emptyList()
                }
            raw.mapNotNull { addr ->
                val line = addr.toReadableString() ?: return@mapNotNull null
                val lat = addr.latitude
                val lon = addr.longitude
                if (lat == 0.0 && lon == 0.0) return@mapNotNull null
                AddressSearchHit(label = line, coordinates = GeoCoordinates(lat, lon))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

/** Returns the first address line, or null if empty. */
internal fun Address.toReadableString(): String? = getAddressLine(0)?.takeIf { it.isNotBlank() }?.let { formatAddressForDisplay(it) }
