package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistResponse
import com.mobile.soundscape.api.dto.MypageArtistRequest
import com.mobile.soundscape.api.dto.MypageGenreRequest
import com.mobile.soundscape.api.dto.MypageNameRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
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
    @GET("api/v1/playlists")
    fun getLibraryPlaylists(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<BaseResponse<LibraryPlaylistResponse>>
}