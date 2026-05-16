package tn.dadadrive.data.network.api

import tn.dadadrive.data.network.model.WalletResponseDto
import tn.dadadrive.data.network.model.WalletTransactionsResponseDto
import retrofit2.http.GET

interface WalletApiService {
    @GET("wallet")
    suspend fun getWallet(): WalletResponseDto

    @GET("wallet/transactions")
    suspend fun getTransactions(): WalletTransactionsResponseDto
}
