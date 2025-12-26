package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.OnboardingSelectedRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OnboardingApi {

    // Onboardign Request
    @POST("onboarding")
    fun sendOnboarding(
        @Body request: OnboardingSelectedRequest
    ): Call<BaseResponse<String>>
}