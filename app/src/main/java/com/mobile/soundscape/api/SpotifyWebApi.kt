package com.mobile.soundscape.api

import com.mobile.soundscape.data.model.music.DeviceResponse
import com.mobile.soundscape.data.model.music.PlayRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query

interface SpotifyWebApi {

    // 1. [추가] 연결된 기기 목록 가져오기
    @GET("v1/me/player/devices")
    fun getAvailableDevices(
        @Header("Authorization") token: String
    ): Call<DeviceResponse>

    // 2. [수정] 재생 명령에 'device_id' 파라미터 추가 (Nullable로 설정)
    @PUT("v1/me/player/play")
    fun playTrack(
        @Header("Authorization") token: String,
        @Body body: PlayRequest,
        @Query("device_id") deviceId: String? = null // 특정 기기를 콕 집어서 재생
    ): Call<Void>
}