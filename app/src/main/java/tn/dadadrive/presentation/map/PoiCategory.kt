package tn.dadadrive.presentation.map

enum class PoiCategory(
    val labelFr: String,
    val hereCategoryId: String
) {
    HOSPITAL("Hôpital", "hospital-health-care-facility"),
    CLINIC("Clinique", "medical-services-clinics"),
    MOSQUE("Mosquée", "mosque"),
    SCHOOL("École / Lycée", "school"),
    BANK("Banque", "bank-financial"),
    KIOSK("Kiosque", "kiosk"),
    GOVERNMENT("Services gouv.", "government-community-facility"),
    CUSTOM_LOCATION("Position personnalisée", "")
}

enum class PoiSearchField {
    PICKUP,
    DROPOFF
}

sealed class PoiSelectionTarget {
    data object Pickup : PoiSelectionTarget()
    data object Destination : PoiSelectionTarget()
    data class IntermediateStop(val index: Int) : PoiSelectionTarget()
}

fun detectCategory(query: String): PoiCategory? {
    val normalized = query.normalizeForPoi()
    if (normalized.isBlank()) return null
    fun containsAny(vararg words: String): Boolean = words.any { normalized.contains(it) }
    return when {
        containsAny("hopital", "hospital", "urgence", "مستشفى") -> PoiCategory.HOSPITAL
        containsAny("clinique", "clinic", "cabinet", "عيادة") -> PoiCategory.CLINIC
        containsAny("mosquee", "mosque", "masjid", "جامع", "مسجد") -> PoiCategory.MOSQUE
        containsAny("ecole", "school", "primaire", "مدرسة", "lycee", "secondaire", "ثانوية") -> PoiCategory.SCHOOL
        containsAny("banque", "bank", "atm", "credit", "بنك") -> PoiCategory.BANK
        containsAny("kiosque", "kiosk", "بقالة") -> PoiCategory.KIOSK
        containsAny("gouvernement", "mairie", "commune", "municipalite", "administration", "daira", "wilaya", "بلدية", "ولاية") -> PoiCategory.GOVERNMENT
        else -> null
    }
}

private fun String.normalizeForPoi(): String = this
    .lowercase()
    .replace("é", "e")
    .replace("è", "e")
    .replace("ê", "e")
    .replace("à", "a")
    .replace("â", "a")
    .replace("î", "i")
    .replace("ï", "i")
    .replace("ô", "o")
    .replace("ù", "u")
    .replace("û", "u")
    .replace("ç", "c")
