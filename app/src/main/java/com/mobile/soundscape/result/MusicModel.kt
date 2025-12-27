package com.mobile.soundscape.result

data class MusicModel(
    // val id: Long,             // 리스트에서 각 아이템을 구별하기 위한 고유 ID (백엔드가 쓸수도?)
    val title: String,          // 음악 제목
    val artist: String,         // 가수 이름
    val albumCover: String,     // 앨범 커버
    val trackUri: String  // 트랙 주소 (기본값은 빈 문자열)
)