package com.mobile.soundscape.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobile.soundscape.R
import com.mobile.soundscape.api.LoginRequest
import com.mobile.soundscape.api.LoginResponse
import com.mobile.soundscape.api.RetrofitClient
import com.mobile.soundscape.api.TokenManager
import com.mobile.soundscape.databinding.ActivityLoginBinding
import com.mobile.soundscape.onboarding.SetnameFragment
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val CLIENT_ID = "2caa74d47f2b40449441b09fbaec95ed"
        private const val REDIRECT_URI = "com.mobile.soundscape://callback"
        private const val REQUEST_CODE = 1337  // 임시 코드
        private const val TAG = "LoginCheck"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.spotifyLoginBtn.setOnClickListener{
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

    private fun startSpotifyLogin() {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.CODE, // 인증 코드 방식
            REDIRECT_URI
        )
        builder.setScopes(arrayOf("streaming", "user-read-email"))
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
     * Retrofit을 사용하여 백엔드 서버로 Auth Code를 보내는 함수
     */

    private fun sendCodeToBackend(code: String) {
        Toast.makeText(this, "서버와 통신 중...", Toast.LENGTH_SHORT).show()

        // 여기서 앱이 죽는지 확인하기 위해 try-catch 추가
        try {
            val loginRequest = LoginRequest(code = code)

            RetrofitClient.api.loginSpotify(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        // 로그 - 서버가 뭘 줬는지 확인
                        Log.d(TAG, "서버 응답 성공: $loginResponse")

                        // 완전 로그인 성공했을 때!!!!!!!!!!
                        if (loginResponse?.accessToken != null) {
                            // 받은 토큰을 내부 저장소에 영구 저장
                            // TODO: 토큰 매니저를 사용해서 토큰을 내부 저장소에 영구 저장
                            // TODO: 백엔드에서 토큰을 불러와서 다른 액티비티에서도 사용할 수 있도록
                            // TokenManager.saveToken(applicationContext, loginResponse.accessToken)
                            
                            // 토스트 메시지 띄우기
                            Toast.makeText(applicationContext, "로그인 성공! 토큰 획득, 메인으로 이동", Toast.LENGTH_LONG).show()

                            // 로그인 성공하면 현재 화면 위에 프래그먼트 띄우기 -> SetnameFragment로 이동
                            val fragment = SetnameFragment()
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.replace(R.id.onboarding_fragment_container, fragment)
                            transaction.addToBackStack(null)


                        } else {
                            Log.e(TAG, "토큰이 비어있음 (null)")
                        }
                    } else {
                        // ★ 로그 추가: 서버가 400/500 에러를 줬을 때
                        Log.e(TAG, "서버 에러 코드: ${response.code()} / 메시지: ${response.errorBody()?.string()}")
                        Toast.makeText(applicationContext, "서버 에러: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // (중요) JSON 형식이 안 맞거나 연결 안 되면 이 로그 뜸
                    Log.e(TAG, "통신 완전 실패 (onFailure): ${t.message}")
                    t.printStackTrace() // 에러 위치를 로그에 찍음
                    Toast.makeText(applicationContext, "통신 실패: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: Exception) {
            // Retrofit 객체 생성 자체가 실패했을 때
            Log.e(TAG, "Retrofit 실행 전 에러: ${e.message}")
            e.printStackTrace()
        }
    }
}

