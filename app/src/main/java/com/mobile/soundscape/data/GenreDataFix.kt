package com.mobile.soundscape.data

import com.mobile.soundscape.onboarding.ArtistData
import com.mobile.soundscape.onboarding.GenreData

object GenreDataFix {
    fun getGenreList(): MutableList<GenreData> {
        return mutableListOf(
            GenreData("K-POP"),
            GenreData("Asian-POP"),
            GenreData("Classic"),
            GenreData("JAZZ"),
            GenreData("Indie"),
            GenreData("Soul/R&B"),
            GenreData("K-Hiphop"),
            GenreData("Hip-hop"),
            GenreData("Rock"),
            GenreData("EDM"),
            GenreData("Ballad"),
            GenreData("POP"),
        )
    }
}