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
import androidx.core.content.ContentProviderCompat.requireContext
import com.mobile.soundscape.MainActivity
import com.mobile.soundscape.R
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LoginRequest
import com.mobile.soundscape.api.dto.LoginResponse
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.data.TokenManager
import com.mobile.soundscape.databinding.ActivityLoginBinding
import com.mobile.soundscape.evaluation.EvaluationActivity
import com.mobile.soundscape.onboarding.SetnameFragment
import com.mobile.soundscape.result.PlaylistResultActivity
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val CLIENT_ID = "2caa74d47f2b40449441b09fbaec95ed"  // 희구님
        private const val REDIRECT_URI = "com.mobile.soundscape://callback"
        private const val REQUEST_CODE = 1337  // 임시 코드
        private const val TAG = "PlayTest"
    }

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

        binding.spotifyLoginBtn.setOnClickListener {
            startSpotifyLogin()
        }

        binding.moveOnboardingButton.setOnClickListener {
            val fragment = SetnameFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.onboarding_fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

    /* --- 스포티파이 로그인 실행 --- */
    private fun startSpotifyLogin() {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.CODE, // 인증 코드 방식
            REDIRECT_URI
        )

        builder.setScopes(arrayOf(
            "user-read-email",
            "user-read-private",
            "user-modify-playback-state", // 재생 명령용
            "user-read-playback-state"    // 현재 상태 확인용
        ))


        val request = builder.build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }


    // 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)

            when (response.type) {
                AuthorizationResponse.Type.CODE -> {
                    val authCode = response.code
                    Log.d(TAG, "1단계 성공 - Auth Code 받음: $authCode")

                    // 받은 코드를 백엔드로 전송하는 함수 호출
                    sendCodeToBackend(authCode)
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "로그인 에러: ${response.error}")
                    Toast.makeText(this, "로그인 에러", Toast.LENGTH_SHORT).show()
                }

                else -> Log.w(TAG, "로그인 취소됨")
            }
        }
    }

    /**
     * Retrofit을 사용하여 백엔드 서버로 엑세스 토큰 보내는 코드
     */
    /* 3. 백엔드로 Auth Code 전송 및 토큰 발급 */
    private fun sendCodeToBackend(authCode: String) {
        RetrofitClient.loginApi.loginSpotify(LoginRequest(code = authCode))
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

    // 간단한 토스트 메시지 헬퍼
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
