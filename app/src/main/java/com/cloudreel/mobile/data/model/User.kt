package com.cloudreel.mobile.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("organization")
    val organization: String? = null,

    @SerializedName("sessionToken")
    val sessionToken: String? = null,

    @SerializedName("tenants")
    val tenants: List<Tenant> = emptyList()
)

data class Tenant(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("channels")
    val channels: List<Channel> = emptyList()
)

data class Channel(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
)
