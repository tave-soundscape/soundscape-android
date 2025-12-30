package com.mobile.soundscape.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.mobile.soundscape.MainActivity
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.ActivityLoginBinding
import com.mobile.soundscape.evaluation.EvaluationActivity
import com.mobile.soundscape.onboarding.SetnameFragment
import kotlin.jvm.java
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "KakaoLogin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.moveHome.setOnClickListener {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.btnEvaluation.setOnClickListener {
            val intent = Intent(this@LoginActivity, EvaluationActivity::class.java)
            startActivity(intent)
        }

        // TODO: 카카오 oauth 구현하기
        binding.btnKakaoOauth.setOnClickListener {
            startKakaoLogin()

        }

        binding.moveOnboardingButton.setOnClickListener {
            val fragment = SetnameFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.onboarding_fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }


    /* 로그인 성공 후 분기 처리 */
    // 카카오 로그인하고 온보딩 한 이력있으면 홈으로
    private fun startKakaoLogin() {
        // 로그인 공통 콜백 (카카오톡으로 하든, 웹으로 하든 결과는 여기로 옴)
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
                Toast.makeText(this, "카카오 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")

                // ★ 카카오에서 받은 토큰을 백엔드로 전송!
                sendKakaoTokenToBackend(token.accessToken)
            }
        }

        // 카카오톡 앱이 설치되어 있으면 카카오톡으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 '취소'를 누른 경우엔 웹 로그인을 시도하지 않고 종료
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡 로그인 실패 시(설치 안 됨 등), 웹(계정)으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")

                    // ★ 카카오에서 받은 토큰을 백엔드로 전송!
                    sendKakaoTokenToBackend(token.accessToken)
                }
            }
        } else {
            // 카카오톡 없으면 바로 웹으로 로그인 시도
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    // 백엔드 서버에 토큰 전송
    private fun sendKakaoTokenToBackend(kakaoAccessToken: String) {
        // 이전 질문에서 정의한 LoginRequest(accessToken = ...) 사용
        val request = LoginRequest(code = kakaoAccessToken)

        RetrofitClient.loginApi.loginKakao(request).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()

                    // 백엔드 로직 성공 (SUCCESS)
                    if (body != null && body.result == "SUCCESS") {
                        val loginData = body.data

                        if (loginData != null) {
                            Log.d(TAG, "백엔드 로그인 성공! 서버응답 값: $body")
                            handleLoginSuccess(loginData.isOnboarded))
                        }
                    } else {
                        // 통신은 됐지만 비즈니스 로직 실패
                        val errorMsg = body?.message ?: "알 수 없는 서버 오류"
                        Log.e(TAG, "서버 에러: $errorMsg")
                        Toast.makeText(this@LoginActivity, "로그인 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // HTTP 400~500 에러
                    val errorBody = response.errorBody()?.string() // ★ 서버가 보낸 에러 메시지 읽기
                    Log.e(TAG, "서버 에러 코드: ${response.code()}")
                    Log.e(TAG, "서버 에러 내용: $errorBody")
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e(TAG, "네트워크 통신 실패: ${t.message}")
                Toast.makeText(this@LoginActivity, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 로그인 성공 후 분기 처리
    private fun handleLoginSuccess(isOnboarded: Boolean) {
        if (isOnboarded) {
            // [CASE A] 이미 가입하고 온보딩도 한 유저 -> 메인 화면으로
            val intent = Intent(this, MainActivity::class.java)
            // 뒤로가기 누르면 로그인 화면 안 나오게 스택 정리
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            // finish()
        } else {
            // [CASE B] 처음 가입한 유저 (온보딩 필요) -> 온보딩 프래그먼트 표시
            // (LoginActivity 내에 fragment_container가 있다고 가정)
            val fragment = SetnameFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment_container, fragment) // ID 확인 필요
                .commit()

            // 혹은 온보딩 액티비티가 따로 있다면 startActivity로 이동
        }
    }
}

