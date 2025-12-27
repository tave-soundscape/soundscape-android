package com.mobile.soundscape.api.client

import android.content.Context
import com.mobile.soundscape.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 토큰 매니저 사용해서 토큰 꺼내기
        val token = TokenManager.getAccessToken(context)

        // 토큰이 없으면(null) 그냥 보냄
        if (token.isNullOrEmpty()){
            return chain.proceed(originalRequest)
        }

        // 토큰이 있으면 헤더에 추가
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}