package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

data class ExploreRequest(
    // 장소 필터: gym, cafe, moving, library, home, park, co-working
    @SerializedName("location")
    val location: String? = null,

    // 소음 필터: quiet, moderate, loud
    @SerializedName("decibel")
    val decibel: String? = null,

    // 목표 필터: focus, relax, sleep, active, anger, consolation, neutral
    @SerializedName("goal")
    val goal: String? = null,

    // 추가적으로 페이징 처리가 필요하다면 포함
    @SerializedName("page")
    val page: Int? = 0
)

data class ExploreResponse(
    val status: String,
    val data: List<PlaceDetail>
)

data class PlaceDetail(
    val id: Long,
    val title: String, // 장소 이름
    val description: String,
    val location: String, // 서버에서 리턴해주는 카테고리 값
    val averageDecibel: String,
    val primaryGoal: String,
    val imageUrl: String? // 장소 이미지 등
)