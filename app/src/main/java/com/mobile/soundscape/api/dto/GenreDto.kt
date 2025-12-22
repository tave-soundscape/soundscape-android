package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

// 서버로 보낼 장르 선택 요청 바디
data class GenreSelectionRequest(
    @SerializedName("genres")
    val genres: List<SelectedGenreDto>
)

// 개별 장르 정보 DTO
data class SelectedGenreDto(
    @SerializedName("name")
    val name: String
    // 장르는 이미지가 필요 없다면 name만 보냅니다.
    // 만약 필요하다면 @SerializedName("imageUrl") val imageUrl: String 추가
)