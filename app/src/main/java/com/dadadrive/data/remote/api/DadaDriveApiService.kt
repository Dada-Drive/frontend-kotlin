package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.dto.CreateFolderRequestDto
import com.dadadrive.data.remote.dto.FileDto
import com.dadadrive.data.remote.dto.LoginRequestDto
import com.dadadrive.data.remote.dto.LoginResponseDto
import com.dadadrive.data.remote.dto.RenameRequestDto
import com.dadadrive.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DadaDriveApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: LoginRequestDto): LoginResponseDto

    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @GET("files")
    suspend fun getFiles(@Query("parentId") parentId: String? = null): List<FileDto>

    @GET("files/{id}")
    suspend fun getFileById(@Path("id") id: String): FileDto

    @DELETE("files/{id}")
    suspend fun deleteFile(@Path("id") id: String)

    @PUT("files/{id}/rename")
    suspend fun renameFile(@Path("id") id: String, @Body request: RenameRequestDto): FileDto

    @POST("files/folder")
    suspend fun createFolder(@Body request: CreateFolderRequestDto): FileDto
}
