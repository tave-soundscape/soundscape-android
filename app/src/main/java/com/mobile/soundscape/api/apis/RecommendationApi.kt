package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.RecommendationRequest
import com.mobile.soundscape.api.dto.RecommendationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RecommendationApi {
    @POST("recommendations")
    fun sendRecommendations(
        @Body request: RecommendationRequest
    ): Call<BaseResponse<RecommendationResponse>>
}