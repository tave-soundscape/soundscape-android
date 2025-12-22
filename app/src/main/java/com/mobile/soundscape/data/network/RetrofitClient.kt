package com.mobile.soundscape.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //  실제 서버 주소로 변경하거나 테스트용 Mock 서버 주소를 사용
    private const val BASE_URL = "https://your-server-domain.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ApiService 인스턴스: Fragment/ViewModel에서 호출할 API 서비스 객체
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}