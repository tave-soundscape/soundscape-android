package com.mobile.soundscape.recommendation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobile.soundscape.api.dto.RecommendationResponse

class RecommendationViewModel : ViewModel() {
    var place: String = ""
    var decibel: Float = 0.0f
    var goal: String = ""
    fun checkData() {
        Log.d("PlayTest", "현재 저장된 데이터 -> 장소: $place / 데시벨: $decibel / 목표: $goal")
    }

    // 서버에서 받은 플레이리스트 결과를 저장하는 변수
    val currentPlaylist = MutableLiveData<RecommendationResponse>()
}