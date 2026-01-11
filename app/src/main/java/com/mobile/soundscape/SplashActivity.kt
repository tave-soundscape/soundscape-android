package com.mobile.soundscape

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mobile.soundscape.data.TokenManager
import com.mobile.soundscape.login.LoginActivity
import com.mobile.soundscape.data.PreferenceManager
import kotlin.jvm.java



class SplashActivity : AppCompatActivity() {

    private val TAG = "PlayTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // 스플래시에서 검사
            if (PreferenceManager.isOnboardingFinished(this)) {
                // 이미 v2 온보딩을 한 사람 -> 토큰 있는지 체크
                checkAutoLogin()
            } else {
                // v2 온보딩을 안 한 사람 (신규 가입자 OR 업데이트한 기존 유저) -> 온보딩으로 이동
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()

        }, 1500)

    }

    // 배포할 때 주석해제
    // Splash 화면에서 액세스 토큰이 유효한지 검사
    private fun checkAutoLogin() {
        // JWT 토큰 꺼내기
        val accessToken = TokenManager.getAccessToken(this)

        if (!accessToken.isNullOrEmpty()) {
            // 토큰이 있다 -> 메인으로 이동
            val intent = Intent(this, MainActivity::class.java)
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