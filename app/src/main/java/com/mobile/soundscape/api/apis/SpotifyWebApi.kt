package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.ArtistSearchResponse
import com.mobile.soundscape.api.dto.PlaylistCoverImageResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyWebApi {
    // 아티스트 검색 API
    @GET("v1/search")
    fun searchArtists(
        @Query("q") query: String,
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = 10,
        @Header("Authorization") token: String // "Bearer {Token}" 형태여야 함
    ): Call<ArtistSearchResponse>

    // 해당 플리 id 요청 -> 라이브러리 4분할 커버 응답
    @GET("v1/playlists/{playlist_id}/images")
    fun getPlaylistCoverImage(
        @Header("Authorization") token: String,
        @Path("playlist_id") playlistId: String
    ): Call<List<PlaylistCoverImageResponse>>

}