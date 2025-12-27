package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.EvaluationRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface EvaluationApi {
    @POST("evaluation")
    fun sendEvaluation(
        @Body request: EvaluationRequest
    ): Call<BaseResponse<String>>
}