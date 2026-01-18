package com.mobile.soundscape.api.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExploreResponse(
    @SerializedName("result") val result: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ExploreDataContent
)

data class ExploreDataContent(
    @SerializedName("playlists") val playlists: List<PlaceDetail>,
    @SerializedName("hasNext") val hasNext: Boolean
)

data class PlaceDetail(
    @SerializedName("playlistId") val id: Int,
    @SerializedName("playlistName") val title: String,

    @SerializedName("imageUrl") val imageUrl: String?,

    @SerializedName("playlistUrl") val playlistUrl: String?,
    @SerializedName("saveCount") val saveCount: Int,
    @SerializedName("songs") val songs: List<Song>? = emptyList(),
    @SerializedName("description") val description: String? = "",
    @SerializedName("location") val location: String? = "",
    @SerializedName("averageDecibel") val averageDecibel: String? = "",
    @SerializedName("primaryGoal") val primaryGoal: String? = "",
    @SerializedName("isSaved") var isSaved: Boolean = false
) : Serializable