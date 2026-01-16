package com.mobile.soundscape.api.apis


import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.ExploreResponse
import com.mobile.soundscape.api.dto.PlaceDetail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExploreApi {

    // 장소별 탐색
    @GET("api/v1/playlists/explore/location/{location}")
    fun getExploreByLocation(
        @Path("location") location: String,
        @Query("page") page: Int = 0
    ): Call<ExploreResponse>

    // 목표별 탐색
    @GET("api/v1/playlists/explore/goal/{goal}")
    fun getExploreByGoal(
        @Path("goal") decibelRange: String,
        @Query("page") page: Int = 0
    ): Call<ExploreResponse>

    // 소음 정도별 탐색
    @GET("api/v1/playlists/explore/decibel/{decibel}")
    fun getExploreByDecibel(
        @Path("decibel") goal: String,
        @Query("page") page: Int = 0
    ): Call<ExploreResponse>

    // ExploreApi.kt 에 추가
    @GET("api/v1/playlists/{playlistId}") // <- 백엔드에서 정한 상세조회 엔드포인트
    fun getExploreDetail(
        @Path("playlistId") playlistId: String
    ): Call<BaseResponse<PlaceDetail>>
}