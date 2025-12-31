package com.mobile.soundscape.api.client

import android.content.Context
import com.mobile.soundscape.data.TokenManager // 패키지 경로 확인하세요!
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()

        // 로그인/회원가입 요청은 토큰이 필요 없음 (오히려 있으면 에러 남!)
        // URL에 "accesstoken" 이나 "login" 같은 단어가 포함되어 있으면 그냥 통과시킴
        if (requestUrl.contains("accesstoken") || requestUrl.contains("login")) {
            return chain.proceed(originalRequest)
        }

        // 토큰 매니저에서 JWT 토큰 꺼내기
        val token = TokenManager.getAccessToken(context)
        // JWT 임시 토큰: val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzY2ODg3ODcyLCJleHAiOjE3OTg0MjM4NzJ9.SMEJfY6YTcfk3bYbnEoa3JHGlSj2Nm49BvZMWbCqoq-0kGN_5az_3GhuYOeP4-d6"

        // 토큰이 없으면 그냥 보냄
        if (token.isNullOrEmpty()){
            return chain.proceed(originalRequest)
        }

        // 토큰이 있으면 헤더에 추가 (Bearer + 띄어쓰기 + 토큰)
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}