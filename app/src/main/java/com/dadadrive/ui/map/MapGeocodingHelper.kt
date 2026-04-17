package com.dadadrive.ui.map

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
 * Forward (HERE suggest + Android Geocoder) and reverse geocoding used by the map route sheet.
 */
internal class MapGeocodingHelper(
    private val context: Context,
    private val searchEngine: SearchEngine?,
    private val getBiasAnchor: () -> GeoCoordinates?
) {

    suspend fun reverseGeocodeLine(latitude: Double, longitude: Double): String? =
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

    suspend fun forwardGeocode(query: String): List<AddressSearchHit> =
        withContext(Dispatchers.IO) {
            val here = forwardGeocodeHereSuggest(query)
            if (here.isNotEmpty()) return@withContext here
            forwardGeocodeAndroid(query)
        }

    private suspend fun forwardGeocodeHereSuggest(query: String): List<AddressSearchHit> {
        val engine = searchEngine ?: return emptyList()
        val anchor = getBiasAnchor()
            ?: GeoCoordinates(
                MapViewModelConstants.DEFAULT_SEARCH_BIAS_LAT,
                MapViewModelConstants.DEFAULT_SEARCH_BIAS_LNG
            )
        return suspendCancellableCoroutine { cont ->
            val textQuery = TextQuery(query, TextQuery.Area(anchor))
            val options = SearchOptions().apply {
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
                        responseDetails: ResponseDetails?
                    ) {
                        if (!cont.isActive) return
                        if (searchError != null) {
                            cont.resume(emptyList())
                            return
                        }
                        val hits = suggestions.orEmpty().mapNotNull { s ->
                            val place = s.place ?: return@mapNotNull null
                            val geo = place.geoCoordinates ?: return@mapNotNull null
                            val addrText = place.address?.addressText?.trim().orEmpty()
                            val label = addrText.takeIf { it.isNotBlank() }
                                ?: s.title.takeIf { !it.isNullOrBlank() } ?: return@mapNotNull null
                            AddressSearchHit(label = label, coordinates = geo)
                        }.distinctBy { "${it.coordinates.latitude},${it.coordinates.longitude}|${it.label}" }
                        cont.resume(hits)
                    }
                }
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
            val raw: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
internal fun Address.toReadableString(): String? =
    getAddressLine(0)?.takeIf { it.isNotBlank() }
