package com.mobile.soundscape.onboarding

data class ArtistData(
    val name: String,
    val imageResId: String, // 실제 앱에서는 Url String 등을 사용
    var isSelected: Boolean = false
)