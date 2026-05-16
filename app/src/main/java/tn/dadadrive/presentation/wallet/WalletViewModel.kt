package tn.dadadrive.presentation.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.R
import tn.dadadrive.domain.models.WalletInfo
import tn.dadadrive.domain.models.WalletTransaction
import tn.dadadrive.domain.protocols.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
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
                .onFailure { _errorMessage.value = appContext.getString(R.string.wallet_error_load) }

            txResult.onSuccess { _transactions.value = it }
                .onFailure {
                    if (_errorMessage.value == null) {
                        _errorMessage.value = appContext.getString(R.string.wallet_error_transactions)
                    }
                }

            _isLoading.value = false
        }
    }

    fun walletAmountCompact(): String {
        val amount = _wallet.value?.balance ?: 0.0
        return String.format(Locale.US, "%.0f", amount)
    }
}
