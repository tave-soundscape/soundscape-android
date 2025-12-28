package com.mobile.soundscape

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobile.soundscape.data.TokenManager
import com.mobile.soundscape.login.LoginActivity
import kotlin.jvm.java

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        // 3초 딜레이 후 검사 시작
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 1500)
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
}