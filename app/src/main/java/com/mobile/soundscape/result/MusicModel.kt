package com.mobile.soundscape.result

data class MusicModel(
    // val id: Long,             // 리스트에서 각 아이템을 구별하기 위한 고유 ID (권장)
    val title: String,          // 음악 제목
    val artist: String,         // 가수 이름
    val albumCover: String,     // 앨범 커버 (URL 이미지라고 가정, 로컬 리소스면 Int로 변경)
    val streamUrl: String = ""  // 트랙 주소 (요청하신 대로 기본값은 빈 문자열)
)