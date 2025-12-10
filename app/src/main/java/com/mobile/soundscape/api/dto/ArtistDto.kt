package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

// 서버로 보낼 전체 요청 바디
data class ArtistSelectionRequest(
    @SerializedName("artists")
    val artists: List<SelectedArtistDto>
)

// 개별 아티스트 정보 DTO (Data Transfer Object)
data class SelectedArtistDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("imageUrl")
    val imageUrl: String
)