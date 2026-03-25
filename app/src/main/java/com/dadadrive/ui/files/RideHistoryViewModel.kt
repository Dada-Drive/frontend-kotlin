package com.dadadrive.ui.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.domain.model.Ride
import com.dadadrive.domain.usecase.ride.GetRideHistoryUseCase
import com.dadadrive.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RideHistoryUiState(
    val rides: List<Ride> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val currentPage: Int = 0
)

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val getRideHistoryUseCase: GetRideHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideHistoryUiState())
    val uiState: StateFlow<RideHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentPage = 0) }
            when (val result = getRideHistoryUseCase(page = 0)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        rides = result.data,
                        hasMore = result.data.size == 20,
                        currentPage = 0
                    )
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun loadMore() {
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        val nextPage = _uiState.value.currentPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            when (val result = getRideHistoryUseCase(page = nextPage)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        rides = it.rides + result.data,
                        hasMore = result.data.size == 20,
                        currentPage = nextPage
                    )
                }
                is Resource.Error -> _uiState.update { it.copy(isLoadingMore = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
