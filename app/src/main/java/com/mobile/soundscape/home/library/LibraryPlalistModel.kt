package com.mobile.soundscape.home.library

import com.mobile.soundscape.result.MusicModel

data class LibraryPlaylistModel(
    val playlistId: Int,
    val title: String,            // 플레이리스트 제목
    val songs: List<MusicModel>,  // 포함된 곡들
    val songCount: Int,
    val location: String="",
    val goal: String="",

    val mainCoverUrl: String? = null
)