package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // 엑세스 토큰
    @SerializedName("accessToken")
    val accessToken: String,

    // 백엔드가 발급한 JWT Refresh Token (이게 있어야 로그인이 안 풀림)
    @SerializedName("refreshToken")
    val refreshToken: String,

    // 온보딩 여부 체크
    // true = 이미 정보 입력함(홈으로), false = 처음 옴(온보딩으로)
    @SerializedName("isOnboarded")
    val isOnboarded: Boolean

    )

data class LoginRequest(
    @SerializedName("code")
    val code: String
)