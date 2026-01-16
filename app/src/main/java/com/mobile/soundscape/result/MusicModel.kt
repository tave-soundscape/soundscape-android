package com.mobile.soundscape.result

import java.io.Serializable // 추가

data class MusicModel(
    val title: String,
    val artist: String,
    val albumCover: String,
    val trackUri: String?
) : Serializable