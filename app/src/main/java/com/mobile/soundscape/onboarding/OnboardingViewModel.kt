package com.mobile.soundscape.onboarding

import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    // 닉네임 저장소
    var nickname: String = ""

    // 아티스트 저장소 (여러 개니까 리스트)
    var selectedArtists: MutableList<String> = mutableListOf()

    // 장르 저장소
    var selectedGenres: MutableList<String> = mutableListOf()
}