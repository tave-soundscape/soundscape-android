package com.mobile.soundscape.home.library

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistResponse
import com.mobile.soundscape.api.dto.PlaylistDetail
import com.mobile.soundscape.databinding.FragmentLibraryBinding
import com.mobile.soundscape.result.MusicDataProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.map


class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    // 전역 변수로 뺴서 나중에 데이터 갱신 시 사용
    private lateinit var libraryAdapter: LibraryAdapter
    private val TAG = "PlayTest"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        /*
        // 1. 동료의 MusicDataProvider 더미 데이터 가져오기
        val dummySongs = MusicDataProvider.createDummyData()

        // 2. 라이브러리용 리스트 데이터 생성
        val libraryData = listOf(
            LibraryPlaylistModel("플레이리스트 01", dummySongs.take(5)),
            LibraryPlaylistModel("운동할 때 듣는 곡", dummySongs.takeLast(3))
        )

        // 3. 어댑터 설정
        val libraryAdapter = LibraryAdapter(libraryData) { selectedPlaylist ->
            // 1. 상세 페이지로 보낼 데이터를 Bundle에 담기
            val bundle = Bundle().apply {
                putString("title", selectedPlaylist.title)
                // putString("date", selectedPlaylist.date)
                putSerializable("songs", ArrayList(selectedPlaylist.songs))
            }

            // 2. nav_graph에 정의한 action ID를 사용하여 이동
            // findNavController()를 쓰려면 상단에 import androidx.navigation.fragment.findNavController 가 필요합니다.
            findNavController().navigate(R.id.action_libraryFragment_to_libraryDetailFragment, bundle)


        }

        binding.rvPlaylist.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = libraryAdapter
        }
        */

        // ----------------------------

        setupRecyclerView()  // 초기 어탭터 설정
        fetchLibraryPlaylists()  // 서버에서 라이브러리 플레이리스트 가져오기


    }

    private fun setupRecyclerView() {
        // 처음에는 빈 리스트로 어댑터 생성
        libraryAdapter = LibraryAdapter(emptyList()) { selectedPlaylist ->
            val bundle = Bundle().apply {
                putString("title", selectedPlaylist.title)
                // putString("date", selectedPlaylist.date)
                putSerializable("songs", ArrayList(selectedPlaylist.songs))
            }
            findNavController().navigate(R.id.action_libraryFragment_to_libraryDetailFragment, bundle)
        }

        binding.rvPlaylist.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = libraryAdapter
        }
    }

    private fun fetchLibraryPlaylists() {
        // size 파라미터는 기본값 10이 적용되거나, 명시적으로 10을 넣을 수 있습니다.
        RetrofitClient.libraryApi.getLibraryPlaylists().enqueue(object : Callback<BaseResponse<LibraryPlaylistResponse>> {

            override fun onResponse(
                call: Call<BaseResponse<LibraryPlaylistResponse>>,
                response: Response<BaseResponse<LibraryPlaylistResponse>>
            ) {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    val libraryResponse = baseResponse?.data

                    if (libraryResponse != null) {
                        updateUI(libraryResponse.playlists)
                    } else {
                        // 데이터가 null일 경우 처리
                        Log.e(TAG, "Response data is null")
                    }
                } else {
                    Log.e(TAG, "Error: ${response.code()}")
                    Toast.makeText(context, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<LibraryPlaylistResponse>>, t: Throwable) {
                Log.e(TAG, "Network Error: ${t.message}")
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(apiPlaylists: List<PlaylistDetail>) {
        // API 데이터(PlaylistDetail) -> UI 데이터(LibraryPlaylistModel) 변환
        val uiDataList = apiPlaylists.map { detail ->
            LibraryPlaylistModel(
                title = detail.playlistName,
                // API에 노래 목록이 없어서 임시로 빈 리스트 처리 (나중에 상세 조회 API가 따로 있다면 거기서 불러와야 함)
                songs = emptyList()
            )
        }

        libraryAdapter = LibraryAdapter(uiDataList) { selectedPlaylist ->
            val bundle = Bundle().apply {
                putString("title", selectedPlaylist.title)
                putSerializable("songs", ArrayList(selectedPlaylist.songs))
            }
            findNavController().navigate(R.id.action_libraryFragment_to_libraryDetailFragment, bundle)
        }
        binding.rvPlaylist.adapter = libraryAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}