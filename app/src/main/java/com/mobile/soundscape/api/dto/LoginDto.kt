package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // 1. 엑세스 토큰
    @SerializedName("accessToken")
    val accessToken: String,

    // 2. 리프레시 토큰
    @SerializedName("refreshToken")
    val refreshToken: String?,

    // 3. 온보딩 여부 체크
    // true = 이미 정보 입력함(홈으로), false = 처음 옴(온보딩으로)
    @SerializedName("isOnboarded")
    val isOnboarded: Boolean

    )

data class LoginRequest(
    @SerializedName("code")
    val code: String
)