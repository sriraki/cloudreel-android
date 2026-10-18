package com.cloudreel.mobile.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("authType")
    val authType: String = "JANYA"
)

data class LoginResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UserData
)

data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("sessionToken")
    val sessionToken: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("tenants")
    val tenants: List<Tenant> = emptyList()
)

data class SignupRequest(
    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("organization")
    val organization: String? = null
)

data class SignupResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String
)

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}
