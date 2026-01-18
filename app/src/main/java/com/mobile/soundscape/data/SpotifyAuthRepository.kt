package com.mobile.soundscape.data

import android.util.Base64
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

// 서치토큰 요청 API 인터페이스
interface SpotifyAuthService {
    @FormUrlEncoded
    @POST("api/token")
    fun getClientToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Call<SpotifyTokenResponse>
}

// 응답 데이터 클래스
data class SpotifyTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

// 실제 토큰을 가져오는 저장소 (싱글톤)
object SpotifyAuthRepository {

    // 대시보드에서 가져온 본인 키 입력 필수!
    private const val CLIENT_ID = "2caa74d47f2b40449441b09fbaec95ed"
    private const val CLIENT_SECRET = "8cf5431e5d65428bb133d902f8a71344"

    // 스포티파이 공식 인증 서버 주소
    private val authRetrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyAuthService::class.java)

    // 토큰 발급 함수
    fun getSearchToken(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        // "Basic " + Base64(ID:Secret) 만들기
        val authString = "$CLIENT_ID:$CLIENT_SECRET"
        val encodedAuth = "Basic " + Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        authRetrofit.getClientToken(encodedAuth).enqueue(object : Callback<SpotifyTokenResponse> {
            override fun onResponse(
                call: Call<SpotifyTokenResponse>,
                response: Response<SpotifyTokenResponse>
            ) {
                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    if (token != null) {
                        onSuccess(token) // 성공 시 토큰 전달
                    } else {
                        onFailure()
                    }
                } else {
                    onFailure()
                }
            }

            override fun onFailure(call: Call<SpotifyTokenResponse>, t: Throwable) {
                onFailure()
            }
        })
    }
}