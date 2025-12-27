package com.mobile.soundscape.evaluation

import android.util.Log
import androidx.lifecycle.ViewModel
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.EvaluationRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EvaluationViewModel : ViewModel() {

    // 데이터 저장 변수들
    var spotifyUserId: String = ""
    var overallRating: Int = -1
    var dislikeReason: List<String> = emptyList()
    var preferredMood: String = ""
    var lyricsPreference: String = ""
    var userOpinion: String = ""
    var willReuse: Boolean = false

    /**
     * 최종 수집된 데이터를 서버로 전송
     */
    fun submitEvaluation() {
        // DTO 생성
        val request = EvaluationRequest(
            spotifyUserId = spotifyUserId,
            overallRating = overallRating,
            dislikeReason = dislikeReason,
            preferredMood = preferredMood,
            lyricsPreference = lyricsPreference,
            userOpinion = userOpinion,
            willReuse = willReuse
        )

        Log.d("EVALUATION", "전송할 데이터: $request")

        // API 호출
        RetrofitClient.evaluationApi.sendEvaluation(request).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    Log.d("EVALUATION", "설문 제출 성공")
                    // 필요하다면 여기서 LiveData를 업데이트하여 화면을 닫거나 이동
                } else {
                    Log.e("EVALUATION", "제출 실패 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e("EVALUATION", "통신 오류: ${t.message}")
            }
        })
    }
}