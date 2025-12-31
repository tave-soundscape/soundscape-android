package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistDetailResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistResponse
import com.mobile.soundscape.api.dto.MypageArtistRequest
import com.mobile.soundscape.api.dto.MypageGenreRequest
import com.mobile.soundscape.api.dto.MypageNameRequest
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query


// 마이페이지 api
interface MypageApi {

    @PATCH("api/mypage/name")
    fun updateName(
        @Body request: MypageNameRequest
    ): Call<BaseResponse<String>>
    
    @PATCH("api/mypage/fav_artists")
    fun updateArtists(
        @Body request: MypageArtistRequest
    ): Call<BaseResponse<String>>

    @PATCH("api/mypage/fav_genres")
    fun updateGenres(
        @Body request: MypageGenreRequest
    ): Call<BaseResponse<String>>

}

// 라이브러리 api
interface LibraryApi {
    // 라이브러리에서 사용자가 저장한 플리ID들 가져오는 api
    @GET("api/v1/playlists")
    fun getLibraryPlaylists(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<BaseResponse<LibraryPlaylistResponse>>

    // 라이브러리에서 id를 보내면 특정 플레이리스트의 상세 정보를 조회하는 api
    @GET("api/v1/playlists/{playlistId}")
    fun getPlaylistDetail(
        @Path("playlistId") id: String
    ): Call<BaseResponse<LibraryPlaylistDetailResponse>>
}
