package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

data class EvaluationRequest (
    @SerializedName("rating")
    val rating: Int,              // 0:매우나쁨 ~ 4:매우좋음

    @SerializedName("dislikeReason")
    val dislikeReason: String,    // 불만족 이유

    @SerializedName("preferredMood")
    val preferredMood: String,    // 선호 분위기

    @SerializedName("lyricsPreference")
    val lyricsPreference: String, // 가사 선호 여부

    @SerializedName("feedback")      // 기존 userOpinion 또는 comment를 feedback으로 변경
    val feedback: String,

    @SerializedName("willReuse")
    val willReuse: Boolean        // 재사용 의향
)