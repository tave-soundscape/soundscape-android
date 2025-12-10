package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // 1. 엑세스 토큰
    @SerializedName("accessToken")
    val accessToken: String,

    // 2. 리프레시 토큰
    @SerializedName("refreshToken")
    val refreshToken: String?,

    )

data class LoginRequest(
    @SerializedName("code")
    val code: String
)