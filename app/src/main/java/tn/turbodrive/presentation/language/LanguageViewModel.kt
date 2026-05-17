// Équivalent Swift : sélection dans MenuSheet.swift + @Environment(LanguageManager.self)
package tn.turbodrive.presentation.language

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tn.turbodrive.core.language.AppLanguage
import tn.turbodrive.data.storage.LanguagePreferenceStore
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel
    @Inject
    constructor(
        private val languagePreferenceStore: LanguagePreferenceStore,
    ) : ViewModel() {
        private val _selected = MutableStateFlow(languagePreferenceStore.readResolvedLanguage())
        val selected: StateFlow<AppLanguage> = _selected.asStateFlow()

        fun selectLanguage(language: AppLanguage) {
            languagePreferenceStore.persistAndApply(language)
            _selected.value = language
        }

        fun followDeviceLanguage() {
            languagePreferenceStore.clearOverrideAndFollowSystem()
            _selected.value = AppLanguage.SYSTEM
        }
    }
