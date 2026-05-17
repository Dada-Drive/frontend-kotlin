package tn.turbodrive.data.network.api

import retrofit2.Response
import retrofit2.http.GET
import tn.turbodrive.data.network.envelope.ApiResponse
import tn.turbodrive.data.network.model.WalletResponseDto
import tn.turbodrive.data.network.model.WalletTransactionsResponseDto

interface WalletApiService {
    @GET("wallet")
    suspend fun getWallet(): Response<ApiResponse<WalletResponseDto>>

    @GET("wallet/transactions")
    suspend fun getTransactions(): Response<ApiResponse<WalletTransactionsResponseDto>>
}
