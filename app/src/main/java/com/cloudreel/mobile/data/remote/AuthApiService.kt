package com.cloudreel.mobile.data.remote

import com.cloudreel.mobile.data.model.LoginResponse
import com.cloudreel.mobile.data.model.SignupRequest
import com.cloudreel.mobile.data.model.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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

    /** Backend check of the Microsoft id token, called before the MSAL login. */
    @GET("v1/login/validate/token")
    suspend fun validateToken(
        @Header("Authorization") bearerToken: String
    ): Response<ResponseBody>

    @POST("v1/producer/signup")
    suspend fun register(
        @Body request: SignupRequest
    ): Response<SignupResponse>
}
