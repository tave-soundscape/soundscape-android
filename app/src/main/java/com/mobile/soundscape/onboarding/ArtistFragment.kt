package com.mobile.soundscape.onboarding

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.SpotifyClient
import com.mobile.soundscape.api.dto.ArtistSearchResponse
import com.mobile.soundscape.data.TokenManager
import com.mobile.soundscape.databinding.FragmentArtistBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.fragment.app.activityViewModels

class ArtistFragment : Fragment() {

    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    private lateinit var adapter: ArtistAdapter
    private val selectedArtistsMap = mutableMapOf<String, ArtistData>()

    // 검색 딜레이 핸들러
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

        setupRecyclerView()
        updateButtonVisibility()
        fetchInitialArtists()
        setupButtons()
        setupSearchListener() // 검색 및 디자인 로직 포함
    }

    /* --- 리사이클러뷰 설정 --- */
    private fun setupRecyclerView() {
        adapter = ArtistAdapter(emptyList()) { artist, position ->
            handleArtistClick(artist)
        }
        binding.rvArtistList.adapter = adapter
        binding.rvArtistList.layoutManager = GridLayoutManager(requireContext(), 3)
        // 깜빡임 방지 (선택 시 재정렬 애니메이션 제거하려면 아래 주석 해제)
        // (binding.rvArtistList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    /* --- 초기 아티스트 데이터 로드 --- */
    private fun fetchInitialArtists() {
        searchSpotifyArtists("year:2024", limit = 50)
    }

    /* --- [핵심] 클릭 및 재정렬 로직 --- */
    private fun handleArtistClick(artist: ArtistData) {
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

        // [기능 추가] 선택된 아이템을 맨 위로 올리기 (재정렬)
        reorderListMovingSelectionsToTop()

        // 버튼 상태 업데이트
        updateButtonVisibility()
    }

    // 리스트를 재정렬하여 어댑터에 반영하는 함수
    private fun reorderListMovingSelectionsToTop() {
        // 현재 어댑터가 가지고 있는 리스트를 복사 (수정 가능하게)
        val currentList = adapter.artistList.toMutableList()

        // 정렬 로직: isSelected가 true인 것을 앞으로 보냄
        currentList.sortWith(compareByDescending { it.isSelected })

        // 어댑터 갱신
        adapter.updateList(currentList)

        // 스크롤을 맨 위로 올려서 사용자가 선택된 것을 바로 보게 함
        binding.rvArtistList.scrollToPosition(0)
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

    /* --- 리스트 동기화 함수 --- */
    private fun syncSelectionState(list: List<ArtistData>): List<ArtistData> {
        return list.map { artist ->
            if (selectedArtistsMap.containsKey(artist.name)) {
                artist.isSelected = true
                selectedArtistsMap[artist.name] = artist
            } else {
                artist.isSelected = false
            }
            artist
        }
    }

    /* --- [핵심] 검색 및 디자인 로직 수정 --- */
    private fun setupSearchListener() {

        // 1. 키보드 엔터키(검색) 눌렀을 때 텍스트 사라짐 방지 및 키보드 내리기
        binding.searchArtist.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {

                // 검색어 가져오기
                val query = binding.searchArtist.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
                    searchSpotifyArtists(query)
                }

                // 키보드 내리기
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                true // 이벤트 소비함 (텍스트뷰가 기본 동작 안 하도록)
            } else {
                false
            }
        }

        // 2. 텍스트 변경 감지 (디자인 + 자동검색)
        binding.searchArtist.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                // --- UI 디자인 변경 로직 ---
                val background = binding.searchArtist.background as? GradientDrawable
                val strokeWidthPx = (2 * resources.displayMetrics.density).toInt() // 2dp

                if (query.isNotEmpty()) {
                    // [입력 있음]
                    // 1. 테두리 생성 (#303032)
                    background?.setStroke(strokeWidthPx, Color.parseColor("#303032"))

                    // 3. X 버튼 보이기 (ID는 xml에 맞춰서 수정하세요: onboarding_x)
                    binding.searchClear.visibility = View.VISIBLE

                } else {
                    // [입력 없음]
                    // 1. 테두리 제거
                    background?.setStroke(0, 0)

                    // 2. 아이콘 숨기기
                    // binding.ivSearchCheck.visibility = View.GONE
                    binding.searchClear.visibility = View.GONE
                }


                // --- 검색 로직 (Debounce) ---
                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    if (query.isNotEmpty()) {
                        searchSpotifyArtists(query)
                    } else {
                        // 검색어 다 지우면 초기화
                        fetchInitialArtists()
                    }
                }
                searchHandler.postDelayed(searchRunnable!!, 500)
            }
        })
    }

    /* --- 버튼 클릭 리스너 --- */
    private fun setupButtons() {
        // [다음 버튼]
        binding.nextButton.setOnClickListener {
            if (selectedArtistsMap.size == 3) {
                // 뷰모델에 데이터 저장
                val artistNames = selectedArtistsMap.keys.toMutableList()
                viewModel.selectedArtists = artistNames

                moveToGenreFragment()
            }
        }

        // 초기화 버튼
        binding.initButton.setOnClickListener {
            selectedArtistsMap.clear()
            adapter.clearSelection()
            updateButtonVisibility()
            // 초기화 시 맨 위로 스크롤
            binding.rvArtistList.scrollToPosition(0)
        }

        // X 버튼 - 검색어 삭제
        binding.searchClear.setOnClickListener {
            // 텍스트 지우기 (TextWatcher가 감지해서 테두리도 없어짐)
            binding.searchArtist.text.clear()
            // 핸들러 콜백 제거 (이전 검색 요청 취소) -> 유령 텍스트 방지
            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            // 즉시 초기 데이터 로드
            fetchInitialArtists()
        }
    }


    /* --- Spotify API 호출 함수 --- */
    private fun searchSpotifyArtists(query: String, limit: Int = 20) {
        val accessToken = TokenManager.getSpotifyToken(requireContext())
        if (accessToken.isNullOrEmpty()) {
            return
        }

        SpotifyClient.api.searchArtists(
            query = query,
            limit = limit,
            token = "Bearer $accessToken"
        ).enqueue(object : Callback<ArtistSearchResponse> {
            override fun onResponse(
                call: Call<ArtistSearchResponse>,
                response: Response<ArtistSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()?.artists?.items ?: emptyList()

                    // 1. API에서 받아온 결과를 데이터 클래스로 변환
                    val apiResultList = items.map { item ->
                        ArtistData(
                            name = item.name,
                            imageResId = item.images.firstOrNull()?.url ?: "",
                            isSelected = false
                        )
                    }

                    // 2. [핵심 로직] 현재 선택된 아티스트 목록 가져오기
                    val mySelectedList = selectedArtistsMap.values.toList()

                    // 3. [병합] (내 선택 목록) + (API 검색 결과) 합치기
                    // distinctBy { it.name } : 이름이 똑같은게 있으면 앞의 것(내 선택)을 남기고 뒤의 것(API 결과)을 버림
                    val combinedList = (mySelectedList + apiResultList).distinctBy { it.name }

                    // 4. 선택 상태 동기화 (병합 과정에서 안전하게 한 번 더 체크)
                    val syncedList = syncSelectionState(combinedList)

                    // 5. 선택된 것이 맨 위로 오도록 정렬
                    val sortedList = syncedList.sortedByDescending { it.isSelected }

                    // 6. 어댑터 업데이트 및 스크롤 초기화
                    adapter.updateList(sortedList)
                    binding.rvArtistList.scrollToPosition(0)

                } else {
                    Log.e("SpotifyAPI", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ArtistSearchResponse>, t: Throwable) {
                Log.e("SpotifyAPI", "Fail: ${t.message}")
            }
        })
    }


    /* --- 화면 이동 --- */
    private fun moveToGenreFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.onboarding_fragment_container, GenreFragment())
            .addToBackStack(null)
            .commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }
}