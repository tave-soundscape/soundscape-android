package com.mobile.soundscape.onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobile.soundscape.R
import com.mobile.soundscape.api.SpotifyClient
import com.mobile.soundscape.api.dto.ArtistSearchResponse
import com.mobile.soundscape.api.dto.ArtistSelectionRequest
import com.mobile.soundscape.api.dto.SelectedArtistDto
import com.mobile.soundscape.data.local.TokenManager
import com.mobile.soundscape.databinding.FragmentArtistBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistFragment : Fragment() {

    // View Binding 설정
    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    // 리사이클러뷰 어댑터
    private lateinit var adapter: ArtistAdapter

    // [핵심] 선택된 아티스트를 기억하는 전역 저장소 (이름을 키로 사용)
    private val selectedArtistsMap = mutableMapOf<String, ArtistData>()

    // 검색 딜레이 핸들러 (과도한 API 호출 방지)
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 리사이클러뷰 설정 (빈 상태로 초기화)
        setupRecyclerView()

        // 2. 초기 화면 상태 설정 (버튼 숨김)
        updateButtonVisibility()

        // 3. [NEW] 초기 아티스트 목록 불러오기 (API 호출)
        fetchInitialArtists()

        // 4. 버튼 클릭 리스너 설정
        setupButtons()

        // 5. 검색창 입력 리스너 설정
        setupSearchListener()
    }

    /* --- 리사이클러뷰 설정 --- */
    private fun setupRecyclerView() {
        // 초기에는 빈 리스트로 어댑터 생성
        adapter = ArtistAdapter(emptyList()) { artist, position ->
            handleArtistClick(artist, position)
        }

        binding.rvArtistList.adapter = adapter
        binding.rvArtistList.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    /* --- [NEW] 초기 아티스트 데이터 로드 (K-Pop 30개 불러오기) --- */
    private fun fetchInitialArtists() {
        // "genre:k-pop"으로 검색하면 인기 있는 K-POP 가수들이 나옵니다.
        // 원하는 다른 키워드가 있다면 "year:2024" 등으로 변경 가능합니다.
        searchSpotifyArtists("year:2024", limit = 50)
    }

    /* --- 클릭 처리 로직 --- */
    private fun handleArtistClick(artist: ArtistData, position: Int) {
        if (artist.isSelected) {
            // 이미 선택됨 -> 해제
            artist.isSelected = false
            selectedArtistsMap.remove(artist.name)
        } else {
            // 선택 안 됨 -> 3개 미만일 때만 선택 허용
            if (selectedArtistsMap.size < 3) {
                artist.isSelected = true
                selectedArtistsMap[artist.name] = artist
            } else {
                Toast.makeText(context, "최대 3명까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 어댑터에 변경 알림
        adapter.notifyItemChanged(position)
        // 버튼 상태 업데이트
        updateButtonVisibility()
    }

    /* --- 버튼 상태 업데이트 --- */
    private fun updateButtonVisibility() {
        val count = selectedArtistsMap.size

        if (count == 3) {
            if (binding.nextButton.visibility != View.VISIBLE) {
                binding.run {
                    nextButton.visibility = View.VISIBLE
                    nextEllipse.visibility = View.VISIBLE
                    initButton.visibility = View.VISIBLE

                    nextButton.alpha = 0f; nextButton.animate().alpha(1f).duration = 300
                    nextEllipse.alpha = 0f; nextEllipse.animate().alpha(1f).duration = 300
                    initButton.alpha = 0f; initButton.animate().alpha(1f).duration = 300
                }
            }
        } else {
            binding.run {
                nextButton.visibility = View.GONE
                nextEllipse.visibility = View.GONE
                initButton.visibility = View.GONE
            }
        }
    }

    /* --- 리스트 동기화 함수 (선택 상태 보존) --- */
    private fun syncSelectionState(list: List<ArtistData>): List<ArtistData> {
        return list.map { artist ->
            if (selectedArtistsMap.containsKey(artist.name)) {
                artist.isSelected = true
                // 맵에 있는 정보도 최신 정보(이미지 등)로 업데이트
                selectedArtistsMap[artist.name] = artist
            } else {
                artist.isSelected = false
            }
            artist
        }
    }

    /* --- 검색 로직 --- */
    private fun setupSearchListener() {
        binding.searchArtist.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    if (query.isNotEmpty()) {
                        searchSpotifyArtists(query)
                    } else {
                        // 검색어가 지워지면 다시 초기 데이터 로드 (또는 저장해둔 리스트 사용 가능)
                        fetchInitialArtists()
                    }
                }
                searchHandler.postDelayed(searchRunnable!!, 500)
            }
        })
    }

    /* --- Spotify API 호출 함수 --- */
    private fun searchSpotifyArtists(query: String, limit: Int = 20) {
        val accessToken = TokenManager.getAccessToken(requireContext())
        if (accessToken.isNullOrEmpty()) {
            Toast.makeText(context, "토큰이 없습니다. 재로그인 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // SpotifyClient.api 혹은 RetrofitClient.spotifyApi 확인 필요
        SpotifyClient.api.searchArtists(
            query = query,
            limit = limit, // limit 파라미터 추가해서 30개 받아오도록 설정
            token = "Bearer $accessToken"
        ).enqueue(object : Callback<ArtistSearchResponse> {
            override fun onResponse(
                call: Call<ArtistSearchResponse>,
                response: Response<ArtistSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()?.artists?.items ?: emptyList()

                    val searchList = items.map { item ->
                        ArtistData(
                            name = item.name,
                            imageResId = item.images.firstOrNull()?.url ?: "",
                            isSelected = false
                        )
                    }

                    // 화면에 뿌리기 전 선택 상태 동기화
                    val syncedList = syncSelectionState(searchList)
                    adapter.updateList(syncedList)
                } else {
                    Log.e("SpotifyAPI", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ArtistSearchResponse>, t: Throwable) {
                Log.e("SpotifyAPI", "Fail: ${t.message}")
            }
        })
    }

    /* --- 버튼 클릭 리스너 --- */
    private fun setupButtons() {
        // [다음 버튼]
        binding.nextButton.setOnClickListener {
            if (selectedArtistsMap.size == 3) {
                // 선택된 아티스트 리스트
                val finalSelection = selectedArtistsMap.values.toList()

                // 백엔드 전송 (필요 시 주석 해제)
                // sendArtistsToBackend(finalSelection)

                moveToGenreFragment()
            }
        }

        // [초기화 버튼]
        binding.initButton.setOnClickListener {
            selectedArtistsMap.clear()
            adapter.clearSelection()
            updateButtonVisibility()
        }
    }

    /* --- 화면 이동 --- */
    private fun moveToGenreFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.onboarding_fragment_container, GenreFragment())
            .addToBackStack(null)
            .commit()
    }

    /**
     * [백엔드 통신 함수]
     * 현재 상태: 백엔드 연결 코드는 주석 처리됨.
     */
    private fun sendArtistsToBackend(selectedArtists: List<ArtistData>) {

        // DTO 변환 확인용 로그
        val dtoList = selectedArtists.map { artist ->
            SelectedArtistDto(name = artist.name, imageUrl = artist.imageResId)
        }
        val requestBody = ArtistSelectionRequest(artists = dtoList)
        Log.d("TEST_MODE", "백엔드 요청 데이터: $requestBody")

        // [테스트 모드] 바로 이동
        Toast.makeText(context, "[테스트] 백엔드 전송 없이 이동", Toast.LENGTH_SHORT).show()
        moveToGenreFragment()

        /* // ▼▼▼ [백엔드 연결 시 주석 해제] ▼▼▼
        // 위쪽 테스트 모드 코드 지우고 아래 주석 해제

        RetrofitClient.api.sendSelectedArtists(requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {
                if (response.isSuccessful) {
                    moveToGenreFragment()
                } else {
                    Toast.makeText(context, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
        // ▲▲▲ [백엔드 연결 시 주석 해제 끝] ▲▲▲
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}