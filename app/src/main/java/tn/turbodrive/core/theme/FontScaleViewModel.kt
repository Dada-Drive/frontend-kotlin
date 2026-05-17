package tn.turbodrive.core.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tn.turbodrive.data.storage.FontScaleRepository
import javax.inject.Inject

@HiltViewModel
class FontScaleViewModel
    @Inject
    constructor(
        private val fontScaleRepository: FontScaleRepository,
    ) : ViewModel() {
        val scaleFactor: StateFlow<Float> =
            fontScaleRepository.scaleFactor
                .stateIn(viewModelScope, SharingStarted.Eagerly, AppFontScalePreference.Default.scaleFactor)

        val preference: StateFlow<AppFontScalePreference> =
            fontScaleRepository.preference
                .stateIn(viewModelScope, SharingStarted.Eagerly, AppFontScalePreference.Default)

        fun setFontScalePreference(value: AppFontScalePreference) {
            viewModelScope.launch { fontScaleRepository.setPreference(value) }
        }
    }
