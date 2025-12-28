package com.mobile.soundscape

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mobile.soundscape.data.TokenManager
import com.mobile.soundscape.login.LoginActivity
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlin.jvm.java

class SplashActivity : AppCompatActivity() {

    private val TAG = "PlayTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d(TAG, "Splash 시작 ")

        Handler(Looper.getMainLooper()).postDelayed({
            moveToLoginActivity()
        }, 1500)

        // 바로 스포티파이 로그인 시작

    }

    private fun moveToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
/*
    private fun authenticateSpotify() {
        Log.d(TAG, "SplashActivity: 스포티파이 인증 요청 시작")

        // 인증 요청 생성
        // AuthorizationResponse.Type.TOKEN -> 이게 핵심! CODE가 아니라 TOKEN을 바로 달라고 함
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )

        // 필요한 권한 설정 (검색 및 재생을 위해 넉넉하게 잡음)
        builder.setScopes(arrayOf(
            "streaming",
            "user-read-email",
            "user-read-private",
            "user-modify-playback-state"
        ))

        // 로그인 창 띄우기 (만약 스포티파이 앱이 깔려있으면 자동 로그인 됨)
        val request = builder.build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    // 로그인 결과 받는 곳
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // 스포티파이 로그인 결과인지 확인
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // 토큰 획득 성공!
                    val accessToken = response.accessToken
                    Log.d(TAG, "SplashActivity: 토큰 획득 성공! 토큰값: ${accessToken.take(10)}...") // 보안상 앞부분만 로그 출력

                    // 토큰 저장 (나중에 검색 API 쓸 때 필요함)
                    TokenManager.saveSpotifyToken(this, accessToken)

                    checkLoginStatus()
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "SplashActivity: 인증 에러 발생 -> ${response.error}")
                    // 에러 나도 일단 앱은 켜져야 하니까 이동은 시킴 (기능 제한됨)
                    checkLoginStatus()
                }

                else -> {
                    // 뒤로가기 누르거나 취소했을 때
                    Log.d(TAG, "SplashActivity: 인증 취소됨 또는 기타 상태")
                    checkLoginStatus()
                }
            }
        }
    }

    private fun checkLoginStatus() {
        // 토큰이 있고 + 저장된 지 55분이 안 지났으면 true
        if (TokenManager.isTokenValid(this)) {

            // [CASE 1] 로그인 유지 -> 메인 화면으로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        } else {

            // [CASE 2] 토큰 없음 or 만료됨 -> 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
        // 스플래시 화면 종료 (뒤로가기 방지)
        finish()
    }

 */
}