package com.mobile.soundscape.explore

data class ExplorePlaylist(
    val id: Int,
    val title: String,
    val description: String,
    val tag: String,
    val coverImage: Int,
    val category: String,
    var isSaved: Boolean = false // 라이브러리 추가 여부
)