package com.mobile.soundscape.playlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.mobile.soundscape.data.network.PlaylistRepository
import com.mobile.soundscape.databinding.FragmentPlaylistDetailBinding // 바인딩 파일명 수정 필요
import com.mobile.soundscape.viewmodels.PlaylistDetailViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class PlaylistDetailFragment : Fragment() {
    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    //  1. Navigation Argument 수신을 위한 준비
    // Navigation 그래프 설정에 따라 자동 생성된 Args 클래스 사용
    private val args: PlaylistDetailFragmentArgs by navArgs()

    //  2. ViewModel 초기화 및 Repository 주입
    private val viewModel: PlaylistDetailViewModel by viewModels {
        PlaylistDetailViewModel.Factory(PlaylistRepository.getInstance())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. 전달받은 플레이리스트 ID 확인 및 데이터 로딩 시작
        val playlistId = args.playlistId
        Log.d("PlaylistDetail", "Received Playlist ID: $playlistId")

        // 로딩 시작 (ViewModel에 ID 전달)
        viewModel.fetchPlaylist(playlistId)

        // 4. ViewModel의 LiveData (StateFlow) 관찰 시작
        observePlaylistDetails()

        // 5. UI 설정 (예: 스포티파이 재생 버튼 설정)
        // binding.spotifyPlayButton.setOnClickListener { /* Spotify 재생 로직 */ }
    }

    private fun observePlaylistDetails() {
        // Fragment 생명 주기에 맞춰 Coroutine Scope 실행
        viewLifecycleOwner.lifecycleScope.launch {
            // StateFlow의 값 변화를 수집 (collect)
            viewModel.playlistDetails.collect { playlist ->
                when {
                    // 데이터가 성공적으로 로딩되었을 때
                    playlist != null -> {
                        //  UI 업데이트
                        binding.tvPlaylistTitle.text = playlist.playListName
                        binding.tvLocationPurpose.text = "${playlist.location} / ${playlist.purpose}"

                        //  곡 목록 표시 (RecyclerView 어댑터에 데이터 전달 필요)
                        Log.d("PlaylistDetail", "Loaded: ${playlist.songs.size} songs")
                        // updateSongList(playlist.songs)

                        // 로딩 UI 숨김
                        // binding.progressBar.visibility = View.GONE
                    }
                    // 로딩 중이거나 초기 상태일 때 (로딩 상태를 따로 Flow로 관리하면 더 좋음)
                    // 현재는 ViewModel이 null을 반환하면 로딩 중이거나 실패로 간주
                    else -> {
                        // binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}