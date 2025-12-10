package com.mobile.soundscape.onboarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.mobile.soundscape.api.dto.GenreSelectionRequest
import com.mobile.soundscape.api.dto.SelectedGenreDto
import com.mobile.soundscape.databinding.FragmentGenreBinding

class GenreFragment : Fragment() {

    // [View Binding 설정]
    private var _binding: FragmentGenreBinding? = null
    private val binding get() = _binding!!

    // 어댑터
    private lateinit var adapter: ArtistAdapter

    // 더미 데이터 (ArtistData 클래스를 재사용 중)
    private val dummyGenreData = mutableListOf<ArtistData>(
        ArtistData("POP", ""), ArtistData("K-POP", ""), ArtistData("FUNK", ""),
        ArtistData("R&B", ""), ArtistData("JAZZ", ""), ArtistData("ROCK", ""),
        ArtistData("HIP-HOP", ""), ArtistData("SOUL", ""), ArtistData("COUNTRY", ""),
        ArtistData("HOUSE", ""), ArtistData("INDIE", ""), ArtistData("J-POP", ""),
        ArtistData("EDM", ""), ArtistData("Lo-fi", "")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기 상태 설정
        updateButtonVisibility(0)

        // 2. 리사이클러뷰 설정
        setupRecyclerView()

        // 3. 버튼 리스너 설정
        setupButtons()

        // 4. 검색 기능 설정
        setupSearchListener()
    }

    private fun setupRecyclerView() {
        adapter = ArtistAdapter(dummyGenreData) {
            // 아이템 클릭 시 선택된 개수 업데이트
            val totalSelectedCount = dummyGenreData.count { it.isSelected }
            updateButtonVisibility(totalSelectedCount)
        }

        binding.rvGenreList.apply {
            this.adapter = this@GenreFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    private fun setupSearchListener() {
        binding.searchGenre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                filterList(searchText)
            }
        })
    }

    private fun filterList(query: String) {
        if (query.isEmpty()) {
            adapter.updateList(dummyGenreData)
        } else {
            val filteredList = dummyGenreData.filter { genre ->
                genre.name.contains(query, ignoreCase = true)
            }
            adapter.updateList(filteredList)
        }
    }

    private fun setupButtons() {
        // 다음 버튼 클릭
        binding.nextButton.setOnClickListener {
            val selectedGenres = dummyGenreData.filter { it.isSelected }

            if (selectedGenres.size == 3) {
                // 백엔드 전송 함수 호출
                sendGenresToBackend(selectedGenres)
            } else {
                Toast.makeText(context, "3가지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 초기화 버튼 클릭
        binding.initButton.setOnClickListener {
            adapter.clearSelection()
            updateButtonVisibility(0)
        }
    }

    /**
     * [백엔드 통신 함수]
     * 현재 상태: 백엔드 연결 코드는 주석 처리됨. 데이터 변환 후 바로 다음 액티비티로 이동.
     */
    private fun sendGenresToBackend(selectedGenres: List<ArtistData>) {

        // 1. [데이터 변환] UI용 데이터를 서버용 DTO로 변환
        val dtoList = selectedGenres.map { genre ->
            SelectedGenreDto(
                name = genre.name
                // imageUrl이 필요하다면 여기에 추가
            )
        }
        val requestBody = GenreSelectionRequest(genres = dtoList)

        // 로그 확인
        Log.d("TEST_MODE", "장르 요청 데이터: $requestBody")

        // 2. [임시 코드] 백엔드 없이 바로 PlaytestActivity로 이동 (테스트 모드)
        // ---------------------------------------------------------------
        Toast.makeText(context, "[테스트] 장르 선택 완료 (서버 전송 생략)", Toast.LENGTH_SHORT).show()
        moveToPlaytestActivity()
        // ---------------------------------------------------------------


        /* // ▼▼▼ [백엔드 연결 시 주석 해제할 부분] ▼▼▼
        // 위쪽의 [임시 코드] 부분을 지우고 이 주석을 해제하세요.

        RetrofitClient.api.sendSelectedGenres(requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    Log.d("API_SUCCESS", "메시지: ${baseResponse?.message}")

                    // 성공 시 다음 액티비티로 이동
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
        // ▲▲▲ [백엔드 연결 시 주석 해제할 부분 끝] ▲▲▲
        */
    }

    private fun moveToPlaytestActivity() {
        val intent = Intent(requireContext(), PlaytestActivity::class.java)
        startActivity(intent)
        // 필요하다면 requireActivity().finish() 추가하여 뒤로가기 방지
    }

    private fun updateButtonVisibility(count: Int) {
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