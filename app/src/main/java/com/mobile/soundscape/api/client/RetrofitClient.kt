package com.mobile.soundscape.api.client

import com.mobile.soundscape.api.apis.LoginApi
import com.mobile.soundscape.api.apis.OnboardingApi
import com.mobile.soundscape.api.apis.RecommendationApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitClient {

    // 일단 로컬 호스트로 설정
    // 로컬에서 실행하기 전에 adb reverse tcp:8080 tcp:8080 터미널에 써서 실행해야함
    private const val BASE_URL = "https://localhost:8080/"

    // Retrofit 객체 생성
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 위에서 만든 retrofit 객체를 재사용해서 여러개의 api 생성
    // 로그인 api
    val loginApi: LoginApi by lazy {
        retrofit.create(LoginApi::class.java)
    }

    // 온보딩 api (서버로 보내는 request만 있음)
    val onboardingApi: OnboardingApi by lazy {
        retrofit.create(OnboardingApi::class.java)
    }

    // (노래추천) 장소, 데시벨, 목표 api
    val recommendationApi: RecommendationApi by lazy {
        retrofit.create(RecommendationApi::class.java)
    }
}