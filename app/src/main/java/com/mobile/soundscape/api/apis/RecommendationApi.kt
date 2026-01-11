package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.RecommendationRequest
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RecommendationApi {
    // 추천받은 거(목표, 데시벨) 보내면 -> playlist로 응답 받음
    @POST("api/v1/playlists")
    fun sendRecommendations(
        @Body request: RecommendationRequest
    ): Call<BaseResponse<RecommendationResponse>>

    // 플레이리스트 이름 수정 요청
    @POST("api/v1/playlists/{playlistId}")
    fun updatePlaylistName(
        @Path("playlistId") id: String,
        @Body request: UpdatePlaylistNameRequest
    ): Call<BaseResponse<String>>
}