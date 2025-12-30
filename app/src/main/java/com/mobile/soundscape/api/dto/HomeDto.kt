package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName


// --- 마이페이지 관련 DTO ---
data class MypageNameRequest (
    @SerializedName("username")
    val username: String
)

data class MypageArtistRequest (
    @SerializedName("artists")
    val artists: List<String>
)

data class MypageGenreRequest (
    @SerializedName("genres")
    val genres: List<String>
)


// --- 라이브러리 관련 DTO ---
data class LibraryPlaylistResponse (
    @SerializedName("playlists")
    val playlists: List<PlaylistDetail>,

    @SerializedName("hasNext")
    val hasNext: Boolean
)

data class PlaylistDetail (
    @SerializedName("playlistId")
    val playlistId: Int,

    @SerializedName("playlistName")
    val playlistName: String
)
