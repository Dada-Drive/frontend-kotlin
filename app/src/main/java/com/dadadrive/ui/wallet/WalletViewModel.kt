package com.dadadrive.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.domain.model.WalletInfo
import com.dadadrive.domain.model.WalletTransaction
import com.dadadrive.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _wallet = MutableStateFlow<WalletInfo?>(null)
    val wallet: StateFlow<WalletInfo?> = _wallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val walletDeferred = async { walletRepository.getWallet() }
            val txDeferred = async { walletRepository.getTransactions() }

            val walletResult = walletDeferred.await()
            val txResult = txDeferred.await()

            walletResult.onSuccess { _wallet.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Failed to load wallet." }

            txResult.onSuccess { _transactions.value = it }
                .onFailure { if (_errorMessage.value == null) _errorMessage.value = it.message ?: "Failed to load transactions." }

            _isLoading.value = false
        }
    }

    fun walletAmountCompact(): String {
        val amount = _wallet.value?.balance ?: 0.0
        return String.format(Locale.US, "%.0f", amount)
    }
}
