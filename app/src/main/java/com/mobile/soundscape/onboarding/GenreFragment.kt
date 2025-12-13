package com.mobile.soundscape.onboarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.RetrofitClient
import com.mobile.soundscape.api.dto.GenreSelectionRequest
import com.mobile.soundscape.api.dto.SelectedGenreDto
import com.mobile.soundscape.databinding.FragmentGenreBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GenreFragment : Fragment() {

    // View Binding
    private var _binding: FragmentGenreBinding? = null
    private val binding get() = _binding!!

    // 어댑터
    private lateinit var adapter: ArtistAdapter

    // [핵심] 선택된 장르를 기억하는 전역 저장소 (이름을 키로 사용)
    private val selectedGenresMap = mutableMapOf<String, ArtistData>()

    // 전체 장르 원본 데이터 (GenreDataFix에서 불러옴)
    private lateinit var originalGenreList: List<ArtistData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 0. 데이터 로드
        originalGenreList = GenreDataFix.getGenreList()

        // 1. 초기 상태 설정
        updateButtonVisibility()

        // 2. 리사이클러뷰 설정
        setupRecyclerView()

        // 3. 버튼 리스너 설정
        setupButtons()

        // 4. 검색 기능 설정
        setupSearchListener()
    }

    private fun setupRecyclerView() {
        // 어댑터 생성 (클릭 로직 연결)
        adapter = ArtistAdapter(originalGenreList) { genre, position ->
            handleGenreClick(genre, position)
        }

        binding.rvGenreList.apply {
            this.adapter = this@GenreFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    // [핵심] 클릭 처리 로직 (Map 사용)
    private fun handleGenreClick(genre: ArtistData, position: Int) {
        if (genre.isSelected) {
            // 이미 선택됨 -> 해제
            genre.isSelected = false
            selectedGenresMap.remove(genre.name)
        } else {
            // 선택 안 됨 -> 3개 미만일 때만 선택 허용
            if (selectedGenresMap.size < 3) {
                genre.isSelected = true
                selectedGenresMap[genre.name] = genre
            } else {
                Toast.makeText(context, "최대 3개까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 어댑터 갱신 & 버튼 업데이트
        adapter.notifyItemChanged(position)
        updateButtonVisibility()
    }

    // [핵심] 리스트 동기화 함수 (검색 시 선택 상태 유지용)
    private fun syncSelectionState(list: List<ArtistData>): List<ArtistData> {
        return list.map { genre ->
            if (selectedGenresMap.containsKey(genre.name)) {
                genre.isSelected = true
            } else {
                genre.isSelected = false
            }
            genre
        }
    }

    private fun setupSearchListener() {
        binding.searchGenre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterList(query)
            }
        })
    }

    private fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalGenreList
        } else {
            originalGenreList.filter { genre ->
                genre.name.contains(query, ignoreCase = true)
            }
        }

        // 화면에 보여주기 전에 선택 상태 동기화(Sync)
        val syncedList = syncSelectionState(filteredList)
        adapter.updateList(syncedList)
    }

    private fun setupButtons() {
        // [다음 버튼]
        binding.nextButton.setOnClickListener {
            // Map에 저장된 3개의 장르 가져오기
            val selectedGenres = selectedGenresMap.values.toList()

            if (selectedGenres.size == 3) {
                // 백엔드 전송
                sendGenresToBackend(selectedGenres)
            } else {
                Toast.makeText(context, "3가지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // [초기화 버튼]
        binding.initButton.setOnClickListener {
            selectedGenresMap.clear() // 저장소 초기화
            adapter.clearSelection()  // UI 초기화
            updateButtonVisibility()  // 버튼 숨김
        }
    }

    /**
     * [백엔드 통신 함수]
     * 현재 상태: 백엔드 연결 코드는 주석 처리됨.
     */
    private fun sendGenresToBackend(selectedGenres: List<ArtistData>) {

        // 1. [데이터 변환]
        val dtoList = selectedGenres.map { genre ->
            SelectedGenreDto(name = genre.name)
        }
        val requestBody = GenreSelectionRequest(genres = dtoList)

        Log.d("TEST_MODE", "장르 요청 데이터: $requestBody")

        // 2. [임시 코드] 테스트 모드 (바로 이동)
        // ---------------------------------------------------------------
        Toast.makeText(context, "[테스트] 장르 선택 완료 -> 이동", Toast.LENGTH_SHORT).show()
        moveToPlaytestActivity()
        // ---------------------------------------------------------------


        /* // ▼▼▼ [백엔드 연결 시 주석 해제] ▼▼▼

        RetrofitClient.api.sendSelectedGenres(requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    moveToPlaytestActivity()
                } else {
                    Log.e("API_ERROR", "코드: ${response.code()}")
                    Toast.makeText(context, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e("API_FAILURE", "에러: ${t.message}")
                Toast.makeText(context, "네트워크 연결 확인 필요", Toast.LENGTH_SHORT).show()
            }
        })
        // ▲▲▲ [백엔드 연결 시 주석 해제 끝] ▲▲▲
        */
    }

    private fun moveToPlaytestActivity() {
        val intent = Intent(requireContext(), PlaytestActivity::class.java)
        startActivity(intent)
    }

    private fun updateButtonVisibility() {
        val count = selectedGenresMap.size // Map 사이즈 기준

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}