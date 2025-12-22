package com.mobile.soundscape.onboarding

object GenreDataFix {
    fun getGenreList(): MutableList<ArtistData> {
        return mutableListOf(
            ArtistData("K-POP", ""),
            ArtistData("J-POP", ""),
            ArtistData("C-POP", ""),
            ArtistData("Indie", ""),
            ArtistData("Soul/R&B", ""),
            ArtistData("K-Hiphop", ""),
            ArtistData("Hip-hop", ""),
            ArtistData("Rock", ""),
            ArtistData("EDM", ""),
            ArtistData("Ballad", ""),
            ArtistData("POP", ""),


            // 더 추가한 것
            ArtistData("FUNK", ""),
            ArtistData("JAZZ", ""),
            ArtistData("HOUSE", ""),
            ArtistData("Lo-fi", ""),
            ArtistData("CLASSIC", ""),
            ArtistData("DISCO", "")
        )
    }
}