package com.mobile.soundscape.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SpotifyClient {

    // 스포티파이 공식 API 주소 (내 로컬 서버 아님!)
    private const val BASE_URL = "https://api.spotify.com/"

    val api: SpotifyWebApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyWebApi::class.java)
    }
}