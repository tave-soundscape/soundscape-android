package com.mobile.soundscape.onboarding

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.EvaluationRequest
import com.mobile.soundscape.api.dto.OnboardingSelectedRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OnboardingViewModel : ViewModel() {
    var nickname: String = ""
    var selectedArtists: MutableList<String> = mutableListOf()
    var selectedGenres: MutableList<String> = mutableListOf()

    // 통신 결과를 프래그먼트에게 알려주기 위한 LiveData
    // true: 성공, false: 실패 (또는 null: 대기)
    private val _onboardingResult = MutableLiveData<Boolean>()
    val onboardingResult: LiveData<Boolean> get() = _onboardingResult

    // 최종 수집된 내용을 서버로 전송
    fun submitOnboarding() {
        // DTO 생성
        val request = OnboardingSelectedRequest(
            nickname = nickname,
            artists = selectedArtists,
            genres = selectedGenres
        )

        // ============================================================
        // [테스트 모드] : 백엔드 없이 성공 처리 (주석 해제/처리 필요)
        // ============================================================
        _onboardingResult.value = true // 바로 성공 신호 보냄
        return
        // ============================================================

/* 실제 서버 통신 주석해제 해서 사용
        // [실제 서버 통신]
        RetrofitClient.onboardingApi.sendOnboarding(request).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    Log.d("API_SUCCESS", "온보딩 정보 전송 성공")
                    // 프래그먼트한테 서버 통신 성공 알림
                    _onboardingResult.value = true
                } else {
                    Log.e("API_ERROR", "전송 실패 코드: ${response.code()}")
                    // 프래그먼트한테 서버 통신 실패 알림
                    _onboardingResult.value = false
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e("API_FAIL", "통신 실패: ${t.message}")
                _onboardingResult.value = false
            }
        })

 */
    }
}