package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.model.WalletResponseDto
import com.dadadrive.data.remote.model.WalletTransactionsResponseDto
import retrofit2.http.GET

interface WalletApiService {
    @GET("wallet")
    suspend fun getWallet(): WalletResponseDto

    @GET("wallet/transactions")
    suspend fun getTransactions(): WalletTransactionsResponseDto
}
