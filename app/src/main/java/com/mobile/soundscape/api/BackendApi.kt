package com.mobile.soundscape.api

import com.mobile.soundscape.api.dto.ArtistSelectionRequest
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.GenreSelectionRequest
import com.mobile.soundscape.api.dto.LoginRequest
import com.mobile.soundscape.api.dto.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BackendApi {
    // @POST: 데이터를 서버로 보낼 때
    // @GET: 데이터 받아올 때

    // --- Login Request ---
    @POST("accesstoken")
    fun loginSpotify(
        @Body request: LoginRequest
    ): Call<BaseResponse<LoginResponse>>

    // --- Artist Request ---
    @POST("artists")
    fun sendSelectedArtists(
        @Body request: ArtistSelectionRequest
    ): Call<BaseResponse<String>>

    // --- Genre Request ---
    @POST("genres")
    fun sendSelectedGenres(
        @Body request: GenreSelectionRequest
    ): Call<BaseResponse<String>>
}