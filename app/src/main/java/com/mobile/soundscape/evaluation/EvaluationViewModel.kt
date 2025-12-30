package com.mobile.soundscape.evaluation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.EvaluationRequest
import kotlinx.coroutines.launch

class EvaluationViewModel : ViewModel() {

    // 1. UI에서 입력받을 데이터들 (변수명은 새 명세에 맞춤)
    var rating: Int = 0
    var dislikeReason: String = ""
    var preferredMood: String = ""
    var lyricsPreference: String = ""
    var feedback: String = ""
    var willReuse: Boolean = false

    // 2. 전송 상태를 알리기 위한 LiveData (Fragment에서 관찰)
    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> get() = _isSuccess

    /**
     * 최종 수집된 데이터를 서버로 전송 (Coroutines 사용)
     */
    fun submitEvaluation() {
        // 새로운 Swagger 명세에 맞춘 DTO 생성
        val request = EvaluationRequest(
            rating = rating,
            dislikeReason = dislikeReason,
            preferredMood = preferredMood,
            lyricsPreference = lyricsPreference,
            feedback = feedback,
            willReuse = willReuse
        )

        Log.d("EVALUATION", "전송 시작: $request")

        // 3. 코루틴을 사용하여 비동기 호출
        viewModelScope.launch {
            try {
                val response = RetrofitClient.evaluationApi.sendEvaluation(request)

                if (response.isSuccessful) {
                    Log.d("EVALUATION", "설문 제출 성공: ${response.body()}")
                    _isSuccess.value = true
                } else {
                    Log.e("EVALUATION", "제출 실패 코드: ${response.code()} / 메시지: ${response.errorBody()?.string()}")
                    _isSuccess.value = false
                }
            } catch (e: Exception) {
                Log.e("EVALUATION", "통신 오류: ${e.message}")
                _isSuccess.value = false
            }
        }
    }
}