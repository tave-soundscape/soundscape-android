package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName
import java.io.Serial

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

    @SerializedName("playlistUrl")
    val playlistUrl: String,

    @SerializedName("songs")
    val songs: List<Song>
)

// class Song
data class Song(
    @SerializedName("title")
    val title: String,

    @SerializedName("artistName")
    val artistName: String,

    @SerializedName("albumName")
    val albumName: String,

    @SerializedName("uri")
    val uri: String,

    @SerializedName("spotifyUrl")
    val spotifyUrl: String,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("duration")
    val duration: String,
)


data class UpdatePlaylistNameRequest(
    // 만약 특정 플리의 ID가 필요하다면 여기에 val playlistId: Int 도 추가해야 함
    @SerializedName("playlistName")
    val newPlaylistName: String
)