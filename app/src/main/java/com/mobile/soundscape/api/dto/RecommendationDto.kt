package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

// 서버로 보낼 때 쓰는 DTO
data class RecommendationRequest (
    @SerializedName("place")
    val place: String,

    @SerializedName("decibel")
    val decibel: Float,

    @SerializedName("goal")
    val goal: String
)


// 서버에서 받는 DTO -> 추천 결과에 따른 플레이리스트
data class RecommendationResponse(
    @SerializedName("playlistName")
    val playlistName: String,

    @SerializedName("placegoal")
    val placeGoal: String,

    @SerializedName("spotifyDeepLink")
    val spotifyDeepLink: String,

    @SerializedName("tracks")
    val tracks: List<MusicDto>
)


// 곡 정보 MusicDto
data class MusicDto(
    @SerializedName("trackUri")
    val trackUri: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: String,

    @SerializedName("albumCover")
    val albumCover: List<AlbumCoverDto> // 앨범 커버도 리스트 형태
)


// 앨범 커버 상세
data class AlbumCoverDto(
    @SerializedName("url")
    val url: String,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int
)
