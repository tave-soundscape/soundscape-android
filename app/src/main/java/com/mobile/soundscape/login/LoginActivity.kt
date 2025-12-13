package com.mobile.soundscape.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobile.soundscape.R
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LoginRequest
import com.mobile.soundscape.api.dto.LoginResponse
import com.mobile.soundscape.api.RetrofitClient
import com.mobile.soundscape.data.local.TokenManager
import com.mobile.soundscape.databinding.ActivityLoginBinding
import com.mobile.soundscape.onboarding.PlaytestActivity
import com.mobile.soundscape.onboarding.SetnameFragment
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


        binding.spotifyLoginBtn.setOnClickListener {
            startSpotifyLogin()
        }

        binding.moveOnboardingButton.setOnClickListener {
            // 1. 이동할 프래그먼트 객체 생성
            val fragment = SetnameFragment()

            // 2. 프래그먼트 매니저를 통해 트랜잭션 시작
            val transaction = supportFragmentManager.beginTransaction()

            // 3. R.id.fragment_container 영역을 fragment로 교체(replace)
            // R.id.fragment_container는 메인 액티비티의 컨테이너 ID여야 함
            transaction.replace(R.id.onboarding_fragment_container, fragment)

            // (선택사항) 뒤로가기 버튼 누르면 다시 돌아오게 하려면 아래 줄 추가
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
    private fun sendCodeToBackend(code: String) {
        Toast.makeText(this, "서버와 통신 중...", Toast.LENGTH_SHORT).show()

        try {
            val loginRequest = LoginRequest(code = code)

            // Retrofit 호출
            RetrofitClient.api.loginSpotify(loginRequest)
                .enqueue(object : Callback<BaseResponse<LoginResponse>> {

                    override fun onResponse(
                        call: Call<BaseResponse<LoginResponse>>,
                        response: Response<BaseResponse<LoginResponse>>
                    ) {
                        if (response.isSuccessful) {
                            // 1. 포장지(BaseResponse) 받기
                            val baseResponse = response.body()

                            // 2. result 확인 안 함! 바로 내용물(data) 꺼내기
                            val loginData = baseResponse?.data

                            // 3. 내용물이 진짜 있는지 확인
                            if (loginData != null) {
                                Log.d(TAG, "서버 응답 성공(내용물): $loginData")

                                // ⭐ 토큰이 비어있지 않으면 찐성공 ‼️‼️‼️‼️‼️‼️‼️
                                if (loginData.accessToken.isNotEmpty()) {

                                    // TODO: 토큰 저장 로직
                                    // TODO: 토큰 매니저 만들어서 앱 전역에서 엑세스 토큰을 사용할 수 있도록함
                                    // ★★★ [여기!] TokenManager를 사용해 토큰 영구 저장 ★★★
                                    // context 자리에는 'this' 또는 'applicationContext'를 넣으면 됩니다.
                                    TokenManager.saveToken(
                                        context = applicationContext,
                                        accessToken = loginData.accessToken,
                                        refreshToken = loginData.refreshToken
                                    )
/*
                                    Log.d(TAG, "토큰 저장 완료! : ${loginData.accessToken}")
                                    Toast.makeText(applicationContext, "로그인 성공! 토큰 획득", Toast.LENGTH_LONG).show()

                                    // 화면 이동
                                    val fragment = SetnameFragment()
                                    val transaction = supportFragmentManager.beginTransaction()
                                    transaction.replace(
                                        R.id.onboarding_fragment_container,
                                        fragment
                                    )
                                    transaction.addToBackStack(null)
                                    transaction.commit()

                                     */

                                    // 일단 임시로 음악 재생되는지 확인하는 페이지로 넘어가기
                                    val intent = Intent(this@LoginActivity, PlaytestActivity::class.java)
                                    startActivity(intent)

                                } else {
                                    Log.e(TAG, "data는 있지만 accessToken이 비어있음")
                                }
                            } else {
                                // HTTP 200이지만 data가 null인 경우 (백엔드 로직 실패)
                                // ex) 백엔드에서 에러 메시지만 보내고 data는 null로 보냈을 때, 가입 불가 등
                                val msg = baseResponse?.message ?: "서버 데이터 없음"
                                Log.e(TAG, "요청 실패 (Data is null): $msg")
                                Toast.makeText(applicationContext, "실패: $msg", Toast.LENGTH_SHORT).show()
                            }

                        } else {
                            // HTTP 통신 오류 (400, 500 등)
                            Log.e(TAG, "서버 에러 코드: ${response.code()} / 메시지: ${response.errorBody()?.string()}")
                            Toast.makeText(applicationContext, "서버 통신 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<BaseResponse<LoginResponse>>, t: Throwable) {
                        Log.e(TAG, "통신 완전 실패 (onFailure): ${t.message}")
                        t.printStackTrace()
                        Toast.makeText(applicationContext, "네트워크 연결 실패", Toast.LENGTH_LONG).show()
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Retrofit 실행 전 에러: ${e.message}")
            e.printStackTrace()
        }
    }
}
