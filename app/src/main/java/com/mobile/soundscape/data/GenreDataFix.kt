package com.mobile.soundscape.data

import com.mobile.soundscape.onboarding.ArtistData
import com.mobile.soundscape.onboarding.GenreData

object GenreDataFix {
    fun getGenreList(): MutableList<GenreData> {
        return mutableListOf(
            GenreData("케이팝"),
            GenreData("아시안팝"),
            GenreData("클래식"),
            GenreData("재즈"),
            GenreData("인디"),
            GenreData("소울/R&B"),
            GenreData("K-힙합"),
            GenreData("힙합"),
            GenreData("락"),
            GenreData("EDM"),
            GenreData("발라드"),
            GenreData("팝"),
        )
    }
}