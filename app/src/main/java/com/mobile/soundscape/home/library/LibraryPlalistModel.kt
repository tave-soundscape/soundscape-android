package com.mobile.soundscape.home.library

import com.mobile.soundscape.result.MusicModel

data class LibraryPlaylistModel(
    val playlistId: Int,
    val title: String,            // 플레이리스트 제목
    val songs: List<MusicModel>,  // 포함된 곡들 (동료분 MusicModel 재사용)
    // val date: String              // 생성 날짜
)