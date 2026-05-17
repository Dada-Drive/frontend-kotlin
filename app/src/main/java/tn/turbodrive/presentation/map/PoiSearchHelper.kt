package tn.turbodrive.presentation.map

import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.search.CategoryQuery
import com.here.sdk.search.Place
import com.here.sdk.search.PlaceCategory
import com.here.sdk.search.ResponseDetails
import com.here.sdk.search.SearchCallbackExtended
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchError
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.SuggestCallbackExtended
import com.here.sdk.search.Suggestion
import com.here.sdk.search.TextQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class PoiSearchHelper(
    private val scope: CoroutineScope,
    private val searchEngineProvider: () -> SearchEngine?,
) {
    private var debounceJob: Job? = null
    private val queryByCategory =
        mapOf(
            PoiCategory.HOSPITAL to "hôpital tunis",
            PoiCategory.CLINIC to "clinique tunis",
            PoiCategory.MOSQUE to "mosquée tunis",
            PoiCategory.SCHOOL to "école tunis",
            PoiCategory.BANK to "banque tunis",
            PoiCategory.KIOSK to "kiosque tunis",
            PoiCategory.GOVERNMENT to "administration tunis",
        )

    // Debounce POI: évite les rafales lors de la saisie.
    fun searchByCategory(
        category: PoiCategory,
        userLocation: GeoCoordinates,
        radius: Int = 35000,
        maxResults: Int = 100,
        onSuccess: (List<Place>) -> Unit,
        onError: (String) -> Unit,
    ) {
        debounceJob?.cancel()
        debounceJob =
            scope.launch {
                delay(500)
                val engine = searchEngineProvider()
                if (engine == null) {
                    onError("Moteur de recherche indisponible")
                    return@launch
                }
                if (category.hereCategoryId.isBlank()) {
                    onSuccess(emptyList())
                    return@launch
                }
                val options =
                    SearchOptions().apply {
                        languageCode = LanguageCode.FR_FR
                        this.maxItems = maxResults
                    }
                val textQuery =
                    TextQuery(
                        "${queryByCategory[category] ?: category.labelFr} grand tunis tunisie",
                        TextQuery.Area(userLocation),
                    )
                engine.search(
                    textQuery,
                    options,
                    object : SearchCallbackExtended {
                        override fun onSearchExtendedCompleted(
                            searchError: SearchError?,
                            items: MutableList<Place>?,
                            responseDetails: ResponseDetails?,
                        ) {
                            val cleaned =
                                items.orEmpty()
                                    .filter { it.geoCoordinates != null }
                                    .distinctBy { place ->
                                        val geo = place.geoCoordinates
                                        "${place.title}|${geo?.latitude}|${geo?.longitude}"
                                    }
                            if (cleaned.isNotEmpty()) {
                                onSuccess(cleaned)
                                return
                            }
                            val categoryQuery =
                                CategoryQuery(
                                    listOf(PlaceCategory(category.hereCategoryId)),
                                    CategoryQuery.Area(
                                        userLocation,
                                        GeoCircle(userLocation, radius.toDouble()),
                                    ),
                                )
                            engine.searchByCategory(categoryQuery, options) { catError: SearchError?, places: MutableList<Place>? ->
                                if (catError != null) {
                                    fallbackSuggestSearch(
                                        engine = engine,
                                        category = category,
                                        anchor = userLocation,
                                        maxResults = maxResults,
                                        onSuccess = onSuccess,
                                        onError = onError,
                                    )
                                    return@searchByCategory
                                }
                                val catCleaned =
                                    places.orEmpty()
                                        .filter { it.geoCoordinates != null }
                                        .distinctBy { place ->
                                            val geo = place.geoCoordinates
                                            "${place.title}|${geo?.latitude}|${geo?.longitude}"
                                        }
                                if (catCleaned.isNotEmpty()) {
                                    onSuccess(catCleaned)
                                } else {
                                    fallbackSuggestSearch(
                                        engine = engine,
                                        category = category,
                                        anchor = userLocation,
                                        maxResults = maxResults,
                                        onSuccess = onSuccess,
                                        onError = onError,
                                    )
                                }
                            }
                        }
                    },
                )
            }
    }

    private fun fallbackSuggestSearch(
        engine: SearchEngine,
        category: PoiCategory,
        anchor: GeoCoordinates,
        maxResults: Int,
        onSuccess: (List<Place>) -> Unit,
        onError: (String) -> Unit,
    ) {
        val fallbackQuery =
            TextQuery(
                "${category.labelFr} Grand Tunis Tunisie",
                TextQuery.Area(anchor),
            )
        val options =
            SearchOptions().apply {
                languageCode = LanguageCode.FR_FR
                this.maxItems = maxResults
            }
        engine.search(
            fallbackQuery,
            options,
            object : SearchCallbackExtended {
                override fun onSearchExtendedCompleted(
                    searchError: SearchError?,
                    items: MutableList<Place>?,
                    responseDetails: ResponseDetails?,
                ) {
                    if (searchError == null) {
                        val cleaned =
                            items.orEmpty()
                                .filter { it.geoCoordinates != null }
                                .distinctBy { place ->
                                    val geo = place.geoCoordinates
                                    "${place.title}|${geo?.latitude}|${geo?.longitude}"
                                }
                        if (cleaned.isNotEmpty()) {
                            onSuccess(cleaned)
                            return
                        }
                    }
                    engine.suggest(
                        fallbackQuery,
                        options,
                        object : SuggestCallbackExtended {
                            override fun onSuggestExtendedCompleted(
                                suggestError: SearchError?,
                                suggestions: MutableList<Suggestion>?,
                                responseDetails: ResponseDetails?,
                            ) {
                                if (suggestError != null) {
                                    onError(suggestError.name)
                                    return
                                }
                                val suggestPlaces =
                                    suggestions.orEmpty()
                                        .mapNotNull { it.place }
                                        .filter { it.geoCoordinates != null }
                                        .distinctBy { place ->
                                            val geo = place.geoCoordinates
                                            "${place.title}|${geo?.latitude}|${geo?.longitude}"
                                        }
                                onSuccess(suggestPlaces)
                            }
                        },
                    )
                }
            },
        )
    }
}
