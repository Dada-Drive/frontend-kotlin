package com.dadadrive.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadSavedTheme())
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    /** `null` = couleur secondaire par défaut du thème clair/sombre. */
    private val _customSecondaryArgb = MutableStateFlow(loadCustomSecondaryArgb())
    val customSecondaryArgb: StateFlow<Int?> = _customSecondaryArgb.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun setCustomSecondaryArgb(argb: Int) {
        _customSecondaryArgb.value = argb
        prefs.edit().putInt(KEY_SECONDARY_ARGB, argb).apply()
    }

    fun clearCustomSecondary() {
        _customSecondaryArgb.value = null
        prefs.edit().remove(KEY_SECONDARY_ARGB).apply()
    }

    private fun loadSavedTheme(): AppTheme {
        val saved = prefs.getString(KEY_THEME, AppTheme.GREEN.name) ?: AppTheme.GREEN.name
        return AppTheme.fromName(saved)
    }

    private fun loadCustomSecondaryArgb(): Int? {
        if (!prefs.contains(KEY_SECONDARY_ARGB)) return null
        return prefs.getInt(KEY_SECONDARY_ARGB, 0)
    }

    companion object {
        private const val PREFS_NAME = "dadadrive_prefs"
        private const val KEY_THEME = "app_theme"
        private const val KEY_SECONDARY_ARGB = "custom_secondary_argb"
    }
}
