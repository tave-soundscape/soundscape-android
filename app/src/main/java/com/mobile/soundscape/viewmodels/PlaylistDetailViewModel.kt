package com.mobile.soundscape.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.soundscape.data.network.PlaylistRepository
import com.mobile.soundscape.home.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(private val repository: PlaylistRepository) : ViewModel() {

    private val _playlistDetails = MutableStateFlow<Playlist?>(null)
    val playlistDetails: StateFlow<Playlist?> = _playlistDetails

    fun fetchPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                // Repository를 호출하여 서버에서 데이터를 가져옴
                val result = repository.getPlaylistDetails(playlistId)
                _playlistDetails.value = result // 성공 시 Flow에 데이터 발행
            } catch (e: Exception) {
                e.printStackTrace()
                // 네트워크 오류 처리 로직 추가 (예: 에러 메시지 UI에 표시)
                _playlistDetails.value = null
            }
        }
    }

    // ViewModelFactory 정의 (ViewModel을 Fragment에 주입하기 위함)
    companion object {
        fun Factory(repository: PlaylistRepository) = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PlaylistDetailViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PlaylistDetailViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}