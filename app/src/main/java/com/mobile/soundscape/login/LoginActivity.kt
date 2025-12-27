package com.mobile.soundscape.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobile.soundscape.MainActivity
import com.mobile.soundscape.R
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LoginRequest
import com.mobile.soundscape.api.dto.LoginResponse
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.data.local.TokenManager
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
        private const val CLIENT_ID = "11f5dcb42f4c4ae2a5f84ea6081abea5"  // 일단 내 아이디로
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
        // 로딩 UI가 있다면 여기서 showLoading()

        RetrofitClient.loginApi.loginSpotify(LoginRequest(code = authCode))
            .enqueue(object : Callback<BaseResponse<LoginResponse>> {

                override fun onResponse(
                    call: Call<BaseResponse<LoginResponse>>,
                    response: Response<BaseResponse<LoginResponse>>
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()?.data

                        if (loginResponse != null && loginResponse.accessToken.isNotEmpty()) {
                            // 토큰 저장 (시간 포함)
                            TokenManager.saveToken(
                                context = applicationContext,
                                accessToken = loginResponse.accessToken,
                                refreshToken = loginResponse.refreshToken
                            )

                            // 2. 온보딩 여부에 따라 화면 이동
                            handleLoginSuccess(loginResponse.isOnboarded)

                        } else {
                            showToast("서버 응답 오류: 데이터가 없습니다.")
                        }
                    } else {
                        Log.e(TAG, "서버 에러: ${response.code()}")
                        showToast("로그인 서버 통신 실패")
                    }
                }

                override fun onFailure(call: Call<BaseResponse<LoginResponse>>, t: Throwable) {
                    Log.e(TAG, "네트워크 오류: ${t.message}")
                    showToast("네트워크 연결을 확인해주세요.")
                }
            })
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
