package com.mobile.soundscape.home.library

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistDetailResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistResponse
import com.mobile.soundscape.api.dto.PlaylistDetail
import com.mobile.soundscape.databinding.FragmentLibraryBinding
import com.mobile.soundscape.result.MusicModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryAdapter: LibraryAdapter

    // 데이터를 관리할 메인 리스트 (수정 가능해야 함)
    private var playlistDataList = mutableListOf<LibraryPlaylistModel>()

    private val TAG = "LibraryFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        setupRecyclerView()
        fetchLibraryPlaylists() // 1. 목록 가져오기 시작
    }

    private fun setupRecyclerView() {
        // 초기 빈 리스트로 어댑터 생성
        libraryAdapter = LibraryAdapter(playlistDataList) { selectedPlaylist ->
            handlePlaylistClick(selectedPlaylist)
        }

        binding.rvPlaylist.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = libraryAdapter

            if (itemDecorationCount > 0) {
                removeItemDecorationAt(0)
            }

            val spacingInPixels = (5 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, 10, true))
        }
    }

    // [Step 1] 전체 플레이리스트 목록(ID, 이름)만 먼저 가져오기
    private fun fetchLibraryPlaylists() {
        RetrofitClient.libraryApi.getLibraryPlaylists(size = 20).enqueue(object : Callback<BaseResponse<LibraryPlaylistResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<LibraryPlaylistResponse>>,
                response: Response<BaseResponse<LibraryPlaylistResponse>>
            ) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.data?.playlists
                    if (playlists != null) {
                        // 2. 껍데기 리스트 만들기 (노래는 아직 비어있음)
                        initPlaylistList(playlists)
                    }
                } else {
                    Log.e(TAG, "List Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BaseResponse<LibraryPlaylistResponse>>, t: Throwable) {
                Log.e(TAG, "List Network Error: ${t.message}")
            }
        })
    }

    // [Step 2] 받아온 목록으로 UI를 먼저 그리고, 상세 조회 반복문 시작
    private fun initPlaylistList(apiPlaylists: List<PlaylistDetail>) {
        playlistDataList.clear()

        // 1. 일단 기본 정보(ID, 제목)만 가지고 모델을 만듭니다. (songs는 빈 리스트)
        apiPlaylists.forEach { detail ->
            playlistDataList.add(
                LibraryPlaylistModel(
                    playlistId = detail.playlistId,
                    title = detail.playlistName,
                    songs = emptyList(), // 아직 모름 (비워둠)
                    songCount = 0
                )
            )
        }

        // 2. 어댑터에 알려서 껍데기라도 먼저 보여줌
        libraryAdapter.notifyDataSetChanged()

        // 3. ★ 핵심: 각 아이템마다 상세 정보(이미지)를 가지러 감
        fetchAllDetails()
    }

    // [Step 3] 반복문을 돌며 상세 API 호출
    private fun fetchAllDetails() {
        // 리스트에 있는 모든 플레이리스트를 순회
        for ((index, playlist) in playlistDataList.withIndex()) {

            // ID를 이용해 상세 조회 API 호출
            RetrofitClient.libraryApi.getPlaylistDetail(playlist.playlistId.toString())
                .enqueue(object : Callback<BaseResponse<LibraryPlaylistDetailResponse>> {

                    override fun onResponse(
                        call: Call<BaseResponse<LibraryPlaylistDetailResponse>>,
                        response: Response<BaseResponse<LibraryPlaylistDetailResponse>>
                    ) {
                        if (!isAdded) return // 프래그먼트가 죽었으면 중단

                        if (response.isSuccessful) {
                            val detailData = response.body()?.data

                            if (detailData != null) {
                                // 성공! 상세 데이터(노래, 커버)를 가져옴
                                updateSinglePlaylist(index, detailData)
                            }
                        } else {
                            Log.e(TAG, "Detail Error ID ${playlist.playlistId}: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<BaseResponse<LibraryPlaylistDetailResponse>>, t: Throwable) {
                        Log.e(TAG, "Detail Network Error: ${t.message}")
                    }
                })
        }
    }

    // [Step 4] 상세 데이터가 도착하면 해당 아이템만 업데이트
    private fun updateSinglePlaylist(index: Int, detailData: LibraryPlaylistDetailResponse) {

        // 1. API Song -> 앱 MusicModel 변환
        val musicList = detailData.songs.map { song ->
            MusicModel(
                title = song.title ?: "",
                artist = song.artistName ?: "",
                albumCover = song.imageUrl ?: "", // ★ 드디어 이미지를 얻음!
                trackUri = song.uri ?: ""
            )
        }

        // 2. 기존 리스트의 해당 인덱스 데이터를 교체 (이미지 포함된 버전으로)
        // 범위 체크 (비동기라 리스트 크기가 달라졌을 수도 있음)
        if (index < playlistDataList.size) {
            val oldItem = playlistDataList[index]

            // 새로운 데이터로 덮어쓰기
            playlistDataList[index] = oldItem.copy(
                songs = musicList,          // 노래 리스트 채워넣기
                songCount = musicList.size
            )

            // 3. 어댑터에게 "이 위치(index) 바뀌었어!" 하고 알림
            // 주의: 어댑터 0번은 '좋아요' 카드이므로, 실제 위치는 index + 1 입니다.
            libraryAdapter.notifyItemChanged(index + 1)
        }
    }

    private fun handlePlaylistClick(selectedPlaylist: LibraryPlaylistModel?) {
        if (selectedPlaylist == null) {
            // 좋아요 클릭
            Toast.makeText(context, "좋아요 목록", Toast.LENGTH_SHORT).show()
        } else {
            // 상세 이동
            val bundle = Bundle().apply {
                putString("playlistId", selectedPlaylist.playlistId.toString())
                putString("title", selectedPlaylist.title)
                // 이미 로딩한 노래 데이터를 넘겨줄 수도 있음
                putSerializable("songs", ArrayList(selectedPlaylist.songs))
            }
            findNavController().navigate(R.id.action_libraryFragment_to_libraryDetailFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}