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

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    private fun loadSavedTheme(): AppTheme {
        val saved = prefs.getString(KEY_THEME, AppTheme.GREEN.name) ?: AppTheme.GREEN.name
        return AppTheme.fromName(saved)
    }

    companion object {
        private const val PREFS_NAME = "dadadrive_prefs"
        private const val KEY_THEME  = "app_theme"
    }
}
