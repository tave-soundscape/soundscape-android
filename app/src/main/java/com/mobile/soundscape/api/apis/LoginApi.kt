package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LoginRequest
import com.mobile.soundscape.api.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call


interface LoginApi {

    // 백엔드로 code 보내서 -> 액세스 토큰 발급받기
    @POST("api/v1/auth/login")
    fun loginSpotify(
        @Body request: LoginRequest
    ): Call<BaseResponse<LoginResponse>>

}