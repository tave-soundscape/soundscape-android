package com.mobile.soundscape.data.network

import com.mobile.soundscape.data.network.ApiService
import com.mobile.soundscape.home.Playlist
import com.mobile.soundscape.data.network.RetrofitClient // RetrofitClient 객체를 import

class PlaylistRepository(private val apiService: ApiService) {

    //  1. 서버에서 플레이리스트 상세 정보를 가져오는 함수
    suspend fun getPlaylistDetails(playlistId: Long): Playlist {
        // RetrofitClient의 apiService를 사용하여 API 호출을 수행합니다.
        // 이 함수는 네트워크 IO 스레드에서 실행되어야 합니다 (ViewModel에서 처리 예정).
        return apiService.getPlaylistDetails(playlistId)
    }

    //  2. (선택적) 로컬 캐싱 로직 등을 여기에 추가할 수 있음

    //  3. API 호출 시 사용할 싱글톤 인스턴스를 제공
    companion object {
        // 이 클래스의 유일한 인스턴스를 저장할 변수
        @Volatile private var instance: PlaylistRepository? = null

        fun getInstance(): PlaylistRepository {
            return instance ?: synchronized(this) {
                instance ?: PlaylistRepository(RetrofitClient.apiService).also { instance = it }
            }
        }
    }
}