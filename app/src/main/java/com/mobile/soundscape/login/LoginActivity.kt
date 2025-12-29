package com.mobile.soundscape.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.mobile.soundscape.api.dto.LoginResponse
import com.mobile.soundscape.data.TokenManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "PlayTest"

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
        val keyHash = Utility.getKeyHash(this)
        Log.d(TAG, "Current KeyHash: $keyHash")

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
        // 카카오계정 로그인 콜백 (결과 처리기)
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")

                // ★ 여기서 백엔드로 토큰 전송!
                // sendCodeToBackend(token.accessToken)
            }
        }

        // 카카오톡 앱이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인 (자동 스위치)
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 '취소'를 누른 경우, 대기하지 않고 웹 로그인을 시도하면 안됨
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")

                    // ★ 여기서 백엔드로 토큰 전송!
                    // sendCodeToBackend(token.accessToken)
                }
            }
        } else {
            // 카카오톡이 설치되어 있지 않은 경우 웹으로 로그인 시도
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    /*
    private fun sendCodeToBackend(authCode: String) {
        RetrofitClient.loginApi.loginKakao(LoginRequest(code = authCode))
            .enqueue(object : Callback<BaseResponse<LoginResponse>> {

                override fun onResponse(
                    call: Call<BaseResponse<LoginResponse>>,
                    response: Response<BaseResponse<LoginResponse>>
                ) {
                    // 1. HTTP 통신 자체는 성공했는지 (200 OK)
                    if (response.isSuccessful) {
                        val body = response.body()

                        // 2. 비즈니스 로직도 성공했는지 (result == "SUCCESS")
                        // 백엔드가 result 필드를 주기로 했으므로 이걸 믿어야 합니다.
                        if (body != null && body.result == "SUCCESS") {
                            val loginResponse = body.data

                            if (loginResponse != null) {
                                // ★ 진짜 로그인 성공!
                                TokenManager.saveToken(
                                    context = applicationContext,
                                    accessToken = loginResponse.accessToken,
                                    refreshToken = loginResponse.refreshToken
                                )
                                handleLoginSuccess(loginResponse.isOnboarded)
                            }
                        } else {
                            // 3. 통신은 됐지만 실패한 경우 (지금 상황)
                            // 서버가 보낸 에러 메시지를 띄워줍니다.
                            val errorMsg = body?.message ?: "알 수 없는 오류"
                            Log.e(TAG, "서버 실패: ${body?.errorCode}")
                            showCustomToast("로그인 실패: $errorMsg")
                        }
                    } else {
                        // 404, 500 등의 에러
                        Log.e(TAG, "HTTP 에러: ${response.code()}")
                        showCustomToast("서버 통신 오류")
                    }
                }

                override fun onFailure(call: Call<BaseResponse<LoginResponse>>, t: Throwable) {
                    Log.e(TAG, "네트워크 오류: ${t.message}")
                    showCustomToast("네트워크 연결을 확인해주세요.")
                }
            })
    }


    fun showCustomToast(message: String, iconResId: Int? = null) {
        val layout = layoutInflater.inflate(R.layout.toast_custom, null)

        val textView = layout.findViewById<TextView>(R.id.tv_toast_message)
        textView.text = message

        val iconView = layout.findViewById<ImageView>(R.id.iv_toast_icon)
        iconView?.visibility = View.GONE // 아이콘이 있으면 GONE 처리


        val toast = Toast(this)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout // 내가 만든 레이아웃을 끼워넣음

        // 위치 조정 (선택사항: 화면 중앙 하단 등)
        toast.setGravity(Gravity.BOTTOM, 0, 300)

        toast.show()
    }

    /* 로그인 성공 후 분기 처리 */
    private fun handleLoginSuccess(isOnboarded: Boolean) {
        if (isOnboarded) {
            // [CASE A] 기존 회원 (온보딩 완료) -> 메인 액티비티로 이동
            val intent = Intent(this, MainActivity::class.java)

            // 뒤로가기 눌렀을 때 로그인 화면 다시 안 나오게 플래그 설정
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish() // 로그인 액티비티 종료

        } else {
            // [CASE B] 신규 회원 (온보딩 미완료) -> 현재 화면에 온보딩 프래그먼트 띄우기
            // 2. 프래그먼트 교체
            val fragment = SetnameFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment_container, fragment)
                .commit()
        }
    }

     */
}
