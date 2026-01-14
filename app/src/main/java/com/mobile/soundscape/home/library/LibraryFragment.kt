package com.mobile.soundscape.home.library

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.client.SpotifyClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistDetailResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistResponse
import com.mobile.soundscape.api.dto.PlaylistCoverImageResponse
import com.mobile.soundscape.api.dto.PlaylistDetail
import com.mobile.soundscape.data.SpotifyAuthRepository
import com.mobile.soundscape.databinding.FragmentLibraryBinding
import com.mobile.soundscape.result.MusicModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.collections.isNullOrEmpty

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryAdapter: LibraryAdapter
    private var playlistDataList = mutableListOf<LibraryPlaylistModel>()

    // 페이지네이션 변수
    private var currentPage = 0       // 보통 0부터 시작 (서버가 1부터라면 1로 변경)
    private val PAGE_SIZE = 10
    private var isLoading = false
    private var isLastPage = false

    private val TAG = "LibraryFragment"
    private var searchToken: String? = null

    // TODO: spotify API로 4분할 사진 직접 불러오기

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        setupRecyclerView()

//        // 처음 실행 시 데이터 로드
//        if (playlistDataList.isEmpty()) {
//            loadLibraryData(0)
//        }

        initTokenAndLoadData()

    }

    private fun setupRecyclerView() {
        libraryAdapter = LibraryAdapter(playlistDataList) { selectedPlaylist ->
            handlePlaylistClick(selectedPlaylist)
        }

        binding.rvPlaylist.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = libraryAdapter

            // 데코레이션 중복 방지
            if (itemDecorationCount > 0) removeItemDecorationAt(0)
            val spacingInPixels = (5 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true))

            // 스크롤 리스너
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // 바닥에 닿았는지 확인
                    if (!recyclerView.canScrollVertically(1) && dy > 0 && !isLoading && !isLastPage) {
                        Log.d(TAG, "스크롤 바닥 감지! 다음 페이지($currentPage + 1) 요청")
                        isLoading = true
                        currentPage++
                        loadLibraryData(currentPage)
                    }
                }
            })
        }
    }

    private fun loadLibraryData(page: Int) {
        isLoading = true

        Log.d(TAG, "API 호출: page=$page, size=$PAGE_SIZE") // 로그로 확인해보세요!

        RetrofitClient.libraryApi.getLibraryPlaylists(page = page, size = PAGE_SIZE)
            .enqueue(object : Callback<BaseResponse<LibraryPlaylistResponse>> {
                override fun onResponse(
                    call: Call<BaseResponse<LibraryPlaylistResponse>>,
                    response: Response<BaseResponse<LibraryPlaylistResponse>>
                ) {
                    if (response.isSuccessful) {
                        val playlists = response.body()?.data?.playlists

                        // 데이터가 없거나 비어있으면 마지막 페이지 처리
                        if (playlists.isNullOrEmpty()) {
                            isLastPage = true
                            isLoading = false
                            Log.d(TAG, "더 이상 불러올 데이터가 없습니다.")
                            return
                        }

                        // 사이즈가 요청한 것보다 적으면 다음 페이지는 없다고 판단
                        if (playlists.size < PAGE_SIZE) {
                            isLastPage = true
                        }

                        // 리스트에 '추가'합니다. (clear 아님)
                        addPlaylistsToAdapter(playlists)

                    } else {
                        Log.e(TAG, "서버 에러: ${response.code()}")
                        isLoading = false
                    }
                }

                override fun onFailure(call: Call<BaseResponse<LibraryPlaylistResponse>>, t: Throwable) {
                    Log.e(TAG, "통신 에러: ${t.message}")
                    isLoading = false
                }
            })
    }

    private fun addPlaylistsToAdapter(newApiPlaylists: List<PlaylistDetail>) {
        val startPosition = playlistDataList.size // 추가되기 전 마지막 위치

        newApiPlaylists.forEach { detail ->
            playlistDataList.add(
                LibraryPlaylistModel(
                    playlistId = detail.playlistId,
                    title = detail.playlistName,
                    songs = emptyList(),
                    songCount = 0,
                    mainCoverUrl = null,
                    location=detail.location,
                    goal=detail.goal

                )
            )
        }

        // 데이터 변경 알림 (전체가 아니라 추가된 부분만)
        // Header(0번)가 있으므로 인덱스는 startPosition + 1 부터 시작
        libraryAdapter.notifyItemRangeInserted(startPosition + 1, newApiPlaylists.size)
        isLoading = false

        // 새로 추가된 아이템들에 대해 상세정보(이미지) 요청
        fetchSpotifyCovers(startPosition, newApiPlaylists)
    }

    // 초기화 및 토큰 발급
    private fun initTokenAndLoadData() {
        SpotifyAuthRepository.getSearchToken(
            onSuccess = { token ->
                searchToken = token

                // ★ 토큰이 준비되었으니 이제 목록을 불러옵니다!
                if (playlistDataList.isEmpty()) {
                    loadLibraryData(0)
                }
            },
            onFailure = {
                Toast.makeText(context, "서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun fetchSpotifyCovers(startIndex: Int, newItems: List<PlaylistDetail>) {
        val token = searchToken ?: return

        for ((i, playlist) in newItems.withIndex()) {
            val globalIndex = startIndex + i
            val spotifyPlaylistId = playlist.spotifyPlaylistId.toString()

            SpotifyClient.api.getPlaylistCoverImage("Bearer $token", spotifyPlaylistId)
                .enqueue(object : Callback<List<PlaylistCoverImageResponse>> {
                    override fun onResponse(
                        call: Call<List<PlaylistCoverImageResponse>>,
                        response: Response<List<PlaylistCoverImageResponse>>
                    ) {
                        // 프래그먼트 죽었으면 중단
                        if (!isAdded) return

                        if (response.isSuccessful) {
                            val images = response.body()
                            // 이미지가 있다면 첫 번째(가장 고화질) URL 사용
                            if (!images.isNullOrEmpty()) {
                                updateSinglePlaylistCover(globalIndex, images[0].url)
                            }
                        } else {
                            Log.e(TAG, "Spotify Image Error: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<List<PlaylistCoverImageResponse>>, t: Throwable) {
                        Log.e(TAG, "Spotify Image Network Error: ${t.message}")
                    }
                })
        }
    }

    private fun updateSinglePlaylistCover(index: Int, imageUrl: String) {
        if (index < playlistDataList.size) {
            val oldItem = playlistDataList[index]

            // 기존 데이터는 유지하고 커버 URL만 업데이트
            playlistDataList[index] = oldItem.copy(
                mainCoverUrl = imageUrl
            )

            // 어댑터 갱신 (헤더 때문에 index + 1)
            libraryAdapter.notifyItemChanged(index + 1)
        }
    }

    private fun handlePlaylistClick(selectedPlaylist: LibraryPlaylistModel?) {
        if (selectedPlaylist == null) {
            Toast.makeText(context, "🔨좋아요 기능은 구현 중입니다", Toast.LENGTH_SHORT).show()
        } else {
            val bundle = Bundle().apply {
                putString("playlistId", selectedPlaylist.playlistId.toString())
                putString("title", selectedPlaylist.title)
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