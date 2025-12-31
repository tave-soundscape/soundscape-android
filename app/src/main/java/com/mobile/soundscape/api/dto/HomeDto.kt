package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName
import com.mobile.soundscape.api.dto.Song


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

// 상세 정보 dto
data class LibraryPlaylistDetailResponse (
    @SerializedName("playlistId")
    val playlistId: Int,

    @SerializedName("playlistName")
    val playlistName: String,

    @SerializedName("playlistUrl")
    val playlistUrl: String,

    @SerializedName("spotifyPlaylistId")
    val spotifyPlaylistId: String,

    @SerializedName("songs")
    val songs: List<Song>
)