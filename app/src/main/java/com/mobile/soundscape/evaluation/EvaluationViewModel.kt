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

    // UI에서 입력받을 데이터들 (변수명은 새 명세에 맞춤)
    var rating: Int = 0
    var dislikeReason: String = ""
    var preferredMood: String = ""
    var lyricsPreference: String = ""
    var feedback: String = ""
    var willReuse: Boolean = false

    // 전송 상태를 알리기 위한 LiveData (Fragment에서 관찰)
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

        // 코루틴을 사용하여 비동기 호출
        viewModelScope.launch {
            try {
                val response = RetrofitClient.evaluationApi.sendEvaluation(request)

                if (response.isSuccessful) {
                    _isSuccess.value = true
                } else {
                    _isSuccess.value = false
                }
            } catch (e: Exception) {
                _isSuccess.value = false
            }
        }
    }
}