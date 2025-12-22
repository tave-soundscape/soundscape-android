package com.mobile.soundscape.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 일단 로컬 호스트로 설정
    // 로컬에서 실행하기 전에 adb reverse tcp:8080 tcp:8080 터미널에 써서 실행해야함
    private const val BASE_URL = "http://localhost:8080/"

    val api: BackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApi::class.java)
    }
}