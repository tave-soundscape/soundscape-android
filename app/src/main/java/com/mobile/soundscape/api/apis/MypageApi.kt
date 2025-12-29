package com.mobile.soundscape.api.apis

import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.MypageArtistRequest
import com.mobile.soundscape.api.dto.MypageGenreRequest
import com.mobile.soundscape.api.dto.MypageNameRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH


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