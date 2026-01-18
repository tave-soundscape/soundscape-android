package com.mobile.soundscape.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LoginRequest
import com.mobile.soundscape.api.dto.LoginResponse
import com.mobile.soundscape.data.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.kakao.sdk.common.util.Utility
import com.mobile.soundscape.data.PreferenceManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var keyHash = Utility.getKeyHash(this)

        // TODO: 카카오 oauth 구현하기
        binding.btnKakaoOauth.setOnClickListener {
            startKakaoLogin()

        }

    }


    /* 로그인 성공 후 분기 처리 */
    // 카카오 로그인하고 온보딩 한 이력있으면 홈으로
    private fun startKakaoLogin() {
        // 로그인 공통 콜백 (카카오톡으로 하든, 웹으로 하든 결과는 여기로 옴)
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(this, "카카오 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                // 카카오에서 받은 토큰을 백엔드로 전송!
                sendKakaoTokenToBackend(token.accessToken)
            }
        }

        // 카카오톡 앱이 설치되어 있으면 카카오톡으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    // 사용자가 '취소'를 누른 경우엔 웹 로그인을 시도하지 않고 종료
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡 로그인 실패 시(설치 안 됨 등), 웹(계정)으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    // 카카오에서 받은 토큰을 백엔드로 전송!
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
        val request = LoginRequest(kakaoAccessToken = kakaoAccessToken)

        RetrofitClient.loginApi.loginKakao(request).enqueue(object : Callback<BaseResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<LoginResponse>>,
                response: Response<BaseResponse<LoginResponse>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()

                    // 백엔드 로직 성공 (SUCCESS)
                    if (body != null && body.result == "SUCCESS") {
                        val loginData = body.data

                        if (loginData != null) {

                            // 백엔드가 준 JWT 토큰을 내부 저장소에 보관
                            TokenManager.saveToken(
                                context = applicationContext,
                                accessToken = loginData.accessToken,
                                refreshToken = loginData.refreshToken
                            )
                            handleLoginSuccess(loginData.isOnboarded)
                        }
                    } else {
                        // 통신은 됐지만 비즈니스 로직 실패
                        val errorMsg = body?.message ?: "알 수 없는 서버 오류"
                        Toast.makeText(this@LoginActivity, "로그인 실패: $errorMsg\n 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // HTTP 400~500 에러
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@LoginActivity, "서버 연결 실패: $errorBody\n 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<LoginResponse>>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 로그인 성공 후 분기 처리
    private fun handleLoginSuccess(serverIsOnboarded: Boolean) {

        // 로컬에 저장된 v2 완려 여부 확인
        val localOnboardingDone = PreferenceManager.isOnboardingFinished(this)

        // 조건 1. 로컬에 v2 키가 있고 & 서버에서도 온보딩 완료면 -> 메인으로 통과
        if (localOnboardingDone && serverIsOnboarded) {
            val intent = Intent(this, MainActivity::class.java)
            // 로그인 화면이 백스택에 남지 않게 클리어
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        else {
            // [그 외 모든 경우] -> 온보딩 진행!
            // case 1: 신규 유저 (local=false, server=false)
            // case 2: 업데이트 유저 (local=false, server=true) -> 강제 재온보딩

            // 프래그먼트 교체 (온보딩 시작)
            val fragment = SetnameFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment_container, fragment)
                .commit()
        }
    }
}

