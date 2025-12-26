package com.mobile.soundscape.evaluation

import androidx.lifecycle.ViewModel

class EvaluationViewModel : ViewModel() {

    // 각 단계별 데이터를 담을 변수들
    var score: Int = -1                  // Step 1: 점수 (1~5)  -1은 아직 평가 전
    var reasons: Set<String> = setOf()  // Step 2: 불만족 이유들
    var atmosphere: String = ""         // Step 3-1: 선호 분위기
    var lyricsPreference: String = ""   // Step 3-2: 가사 유무 선호
    var opinion: String = ""            // Step 4: 주관식 의견
    var reuseIntention: Boolean = false // Step 5: 이용 의향

    /**
     * 최종 수집된 데이터를 서버로 전송하거나 로그로 출력하는 함수
     */
    fun submitEvaluation() {
        // 모든 데이터가 모였을 때 여기서 API 호출을 진행
        val finalData = mapOf(
            "score" to score,
            "reasons" to reasons,
            "atmosphere" to atmosphere,
            "lyrics" to lyricsPreference,
            "opinion" to opinion,
            "reuse" to reuseIntention
        )

        println("최종 제출 데이터: $finalData")
        // Retrofit 등을 사용하여 서버 전송 로직 추가
    }
}