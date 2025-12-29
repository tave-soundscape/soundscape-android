package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName

data class MypageNameRequest (
    @SerializedName("username")
    val username: String
)

data class MypageArtistRequest (
    @SerializedName("artists")
    val artists: List<String>
)

data class MypageGenreRequest (
    @SerializedName("genres")
    val genres: List<String>
)