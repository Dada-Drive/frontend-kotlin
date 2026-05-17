package tn.turbodrive.core.theme

/** Profile → Appearance font scale (design-system.md §3.2), persisted via DataStore. */
enum class AppFontScalePreference(val storageValue: String, val scaleFactor: Float) {
    Small("small", 0.85f),
    Default("default", 1f),
    Large("large", 1.15f),
    ExtraLarge("extraLarge", 1.3f),
    ;

    companion object {
        fun fromStored(raw: String?): AppFontScalePreference = entries.firstOrNull { it.storageValue == raw } ?: Default
    }
}
