package com.mobile.soundscape.data

import com.mobile.soundscape.onboarding.ArtistData
import com.mobile.soundscape.onboarding.GenreData

object GenreDataFix {
    fun getGenreList(): MutableList<GenreData> {
        return mutableListOf(
            GenreData("팝"),
            GenreData("케이팝"),
            GenreData("밴드"),
            GenreData("힙합"),
            GenreData("인디"),
            GenreData("발라드"),
            GenreData("재즈"),
            GenreData("클래식"),
            GenreData("락"),
            GenreData("EDM"),
            GenreData("OST"),
            GenreData("R&B"),
        )
    }
}