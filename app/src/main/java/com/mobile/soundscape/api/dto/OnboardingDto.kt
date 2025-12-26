package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName


data class OnboardingSelectedRequest (
    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("artists")
    val artists: List<String>,

    @SerializedName("genres")
    val genres: List<String>
)

