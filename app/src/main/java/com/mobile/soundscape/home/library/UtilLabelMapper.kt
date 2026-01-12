package com.mobile.soundscape.home.library

object LabelMapper {
    // 1. 장소 매핑 (영어 -> 한국어)
    private val placeMap = mapOf(
        "home" to "집/실내",
        "cafe" to "카페",
        "co-working" to "코워킹",
        "gym" to "헬스장",
        "library" to "도서관",
        "moving" to "이동중",
        "park" to "공원"
    )

    // 2. 목표 매핑 (영어 -> 한국어)
    private val goalMap = mapOf(
        "sleep" to "수면",
        "focus" to "집중",
        "consolation" to "위로",
        "active" to "활력",
        "stabilization" to "안정",
        "anger" to "분노",
        "relax" to "휴식",
        "neutral" to "미선택"
    )

    // 변환 함수 (맵에 있으면 한국어, 없으면 원래 글자 그대로 반환)
    fun getKoreanPlace(raw: String?): String {
        if (raw == null) return ""
        return placeMap[raw] ?: raw
    }

    fun getKoreanGoal(raw: String?): String {
        if (raw == null) return ""
        return goalMap[raw] ?: raw
    }
}