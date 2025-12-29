package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.ArtistSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyWebApi {
    // 아티스트 검색 API
    @GET("v1/search")
    fun searchArtists(
        @Query("q") query: String,
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = 10,
        @Header("Authorization") token: String // "Bearer {Token}" 형태여야 함
    ): Call<ArtistSearchResponse>
}