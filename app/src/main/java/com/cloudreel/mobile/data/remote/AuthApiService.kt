package com.cloudreel.mobile.data.remote

import com.cloudreel.mobile.data.model.LoginResponse
import com.cloudreel.mobile.data.model.SignupRequest
import com.cloudreel.mobile.data.model.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApiService {

    @Multipart
    @POST("v1/login")
    suspend fun login(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody?,
        @Part("authType") authType: RequestBody
    ): Response<LoginResponse>

    @POST("v1/producer/signup")
    suspend fun register(
        @Body request: SignupRequest
    ): Response<SignupResponse>
}
