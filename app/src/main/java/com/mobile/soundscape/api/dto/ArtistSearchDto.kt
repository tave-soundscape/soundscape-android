package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

// 1. 전체 응답 (최상위)
data class ArtistSearchResponse(
    @SerializedName("artists")
    val artists: ArtistSearchResults
)

// 2. 검색 결과 컨테이너 (artists: { ... } 내부)
data class ArtistSearchResults(
    @SerializedName("items")
    val items: List<ArtistSearchItem>
)

// 3. 개별 아티스트 정보
data class ArtistSearchItem(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("images")
    val images: List<ArtistSearchImage>
)

// 4. 이미지 정보
data class ArtistSearchImage(
    @SerializedName("url")
    val url: String,

    @SerializedName("height")
    val height: Int?,

    @SerializedName("width")
    val width: Int?
)