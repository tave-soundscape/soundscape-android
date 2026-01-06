package com.mobile.soundscape

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
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

        val logo = findViewById<ImageView>(R.id.vector)

        // 애니메이션 실행
        logo.animate()
            .scaleX(3f)    // X축으로 1.5배 커짐
            .scaleY(3f)    // Y축으로 1.5배 커짐
            .setDuration(1000) // 1000ms = 1초 동안 실행
            .withEndAction {
            } .start()

        // Log.e(TAG, "accesstoken: ${TokenManager.getAccessToken(this)}")
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
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // 토큰이 없다 -> 로그인으로 이동
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