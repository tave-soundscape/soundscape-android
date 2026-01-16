package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.PlaylistPollingResponse
import com.mobile.soundscape.api.dto.RecommendationRequest
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
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

    // 상세 조회 api
    @GET("api/v1/playlists/{playlistId}")
    fun getPlaylistDetail(
        @Path("playlistId") id: String
    ): Call<BaseResponse<RecommendationResponse>>

    // 사용자가 스포티파이 딥링크를 클릭하면 서버로 보내기
    @POST("api/v1/analytics/{playlistId}")
    fun sendAnalytics(
        @Path("playlistId") id: String,
    ): Call<BaseResponse<String>>


    /* ----------------------------- */
    // 비동기 polling 방식으로 구현

    // 장소,데시벨,목표 보내기 -> 백엔드에서 플리 생성 작업을 비동기로 시작
    @POST("api/v1/playlists/async")
    fun sendPlaylistPolling(
        @Body request: RecommendationRequest
    ): Call<BaseResponse<String>>
    // "data": "플레이리스트 생성 작업이 시작되었습니다. taskId: task_ZND0fTVRk9QOb9g" 이런식으로 응답옴


    // 플레이리스트 생성 작업 상태 확인 API
    @GET("api/v1/playlists/tasks/{taskId}")
    fun getPlaylistPolling(
        @Path("taskId") taskId: String
    ): Call<BaseResponse<PlaylistPollingResponse>>


}