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
            checkAutoLogin()
        }, 1500)

    }

    // 배포할 때 주석해제
    // Splash 화면에서 액세스 토큰이 유효한지 검사
    private fun checkAutoLogin() {
        // JWT 토큰 꺼내기
        val accessToken = TokenManager.getAccessToken(this)

        if (!accessToken.isNullOrEmpty()) {
            // 토큰이 있다 -> 메인으로 이동
            Log.d(TAG, "토큰 있음: 메인으로 이동")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // 토큰이 없다 -> 로그인으로 이동
            Log.d(TAG, "토큰 없음: 로그인으로 이동")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

/*
    private fun moveToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
*/
}