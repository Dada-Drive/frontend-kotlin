package tn.dadadrive.data.network.api

import retrofit2.Response
import retrofit2.http.GET
import tn.dadadrive.data.network.envelope.ApiResponse
import tn.dadadrive.data.network.model.WalletResponseDto
import tn.dadadrive.data.network.model.WalletTransactionsResponseDto

interface WalletApiService {
    @GET("wallet")
    suspend fun getWallet(): Response<ApiResponse<WalletResponseDto>>

    @GET("wallet/transactions")
    suspend fun getTransactions(): Response<ApiResponse<WalletTransactionsResponseDto>>
}
