package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.ExploreResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ExploreApi {
    @GET("api/v1/explore") // 스웨거에 있는 대로 GET 유지
    suspend fun getExploreResults(
        @Query("location") location: String?,
        @Query("decibel") decibel: String?,
        @Query("goal") goal: String?
    ): ExploreResponse
}