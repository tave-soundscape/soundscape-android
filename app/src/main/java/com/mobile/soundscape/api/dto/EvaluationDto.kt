package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

data class EvaluationRequest (
    @SerializedName("spotifyUserId")
    val spotifyUserId: String,

    @SerializedName("score")
    val overallRating: Int,       // 1~5 점수

    @SerializedName("dislikeReason")
    val dislikeReason: List<String>,    // 불만족 이유 (없으면 빈 문자열)

    @SerializedName("preferredMood")
    val preferredMood: String,    // 선호 분위기

    @SerializedName("lyricsPreference")
    val lyricsPreference: String, // 가사 선호 여부

    @SerializedName("comment")
    val userOpinion: String,      // 주관식 의견

    @SerializedName("willReuse")
    val willReuse: Boolean        // 재사용 의향 (true/false)
)