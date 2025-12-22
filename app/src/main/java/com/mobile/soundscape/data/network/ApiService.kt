package com.mobile.soundscape.data.network

import com.mobile.soundscape.home.Playlist // Playlist 데이터 클래스를 import 해야 함
import retrofit2.http.GET
import retrofit2.http.Path

// API 통신을 위한 인터페이스
interface ApiService {

    /**
     * 특정 ID를 가진 플레이리스트의 상세 정보를 가져옵니다.
     * 예: /api/playlists/1001
     */
    @GET("api/playlists/{playlistId}")
    suspend fun getPlaylistDetails(
        // URL Path의 {playlistId} 부분에 Long 타입 ID를 바인딩
        @Path("playlistId") playlistId: Long
    ): Playlist // 응답 JSON을 Playlist 데이터 클래스로 변환
}