package com.mobile.soundscape.onboarding

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
import com.mobile.soundscape.R
import com.mobile.soundscape.api.dto.ArtistSelectionRequest
import com.mobile.soundscape.api.dto.SelectedArtistDto
import com.mobile.soundscape.databinding.FragmentArtistBinding

class ArtistFragment : Fragment() {

    // View Binding 설정
    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    // 리사이클러뷰 어댑터
    private lateinit var adapter: ArtistAdapter

    // 아티스트 더미 데이터
    private val dummyArtistList = mutableListOf<ArtistData>(
        ArtistData("NewJeans", ""), ArtistData("IVE", ""), ArtistData("aespa", ""),
        ArtistData("LE SSERAFIM", ""), ArtistData("IU", ""), ArtistData("BTS", ""),
        ArtistData("BLACKPINK", ""), ArtistData("NCT", ""), ArtistData("SEVENTEEN", ""),
        ArtistData("EXO", ""), ArtistData("TWICE", ""), ArtistData("Red Velvet", "")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 바인딩 초기화 - XML을 인플레이트하여 바인딩 객체 생성
        _binding = FragmentArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)  // 뷰가 생성된 후 로직 시작

        // 1. 초기 화면 상태 설정 (처음에는 선택한게 0개 이므로 버튼 숨김)
        updateButtonVisibility(0)

        // 2. 리사이클러뷰 설정
        setupRecyclerView()

        // 3. 버튼 클릭 리스너 설정
        setupButtons()

        // 4. 검색창 입력 리스너 설정
        setupSearchListener()
    }

    /* --- 리사이클러뷰 설정 --- */
    private fun setupRecyclerView() {
        adapter = ArtistAdapter(dummyArtistList) {
            // [아이템 클릭 콜백]
            // 화면에 보이는 리스트가 아니라 dummyArtistList 전체에서 몇 개가 선택됐는지 갯수 세기
            val totalSelectedCount = dummyArtistList.count { it.isSelected }

            // UI 업데이트 (버튼 표시/숨김)
            updateButtonVisibility(totalSelectedCount)
        }

        binding.rvArtistList.apply{
            this.adapter = this@ArtistFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    /* --- 검색 로직 --- */
    private fun setupSearchListener() {
        binding.searchArtist.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            // 텍스트가 바뀔때마다 업데이트
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                filterList(searchText)
            }
        })
    }

    /* --- 리스트 필터링 로직 --- */
    private fun filterList(query: String) {
        if (query.isEmpty()) {
            // 1. 검색어가 없으면 -> 원본 리스트를 보여줌
            adapter.updateList(dummyArtistList)
        } else {
            // 2. 검색어가 있으면 -> 이름에 검색어가 포함된 애들만 거름 (대소문자 무시)
            val filteredList = dummyArtistList.filter { artist ->
                artist.name.contains(query, ignoreCase = true)
            }
            adapter.updateList(filteredList)

        }
    }

    /* --- 버튼 클릭 리스터 설정 --- */
    private fun setupButtons() {
        // 다음 버튼
        binding.nextButton.setOnClickListener {
            val selectedArtists = dummyArtistList.filter { it.isSelected }

            if (selectedArtists.size == 3) {
                // 백엔드 전송
                sendArtistsToBackend(selectedArtists)
            } else {
                // 굳ㅇㅣ???
                Toast.makeText(context, "3명을 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // [초기화 버튼]
        binding.initButton.setOnClickListener {
            adapter.clearSelection()
            updateButtonVisibility(0)
        }
    }



    /**
     * [백엔드 통신 함수]
     * 현재 상태: 백엔드 연결 코드는 주석 처리됨. 데이터 변환 후 바로 다음 화면으로 이동.
     */
    private fun sendArtistsToBackend(selectedArtists: List<ArtistData>) {

        // 1. [데이터 변환] UI용 데이터를 서버용 DTO로 변환하는 로직은 그대로 둡니다. (잘 동작하는지 확인용)
        val dtoList = selectedArtists.map { artist ->
            SelectedArtistDto(
                name = artist.name,
                imageUrl = artist.imageResId
            )
        }
        val requestBody = ArtistSelectionRequest(artists = dtoList)

        // 로그 찍어서 데이터가 잘 만들어졌는지 확인
        Log.d("TEST_MODE", "생성된 요청 데이터: $requestBody")


        // 2. [임시 코드] 백엔드 없이 바로 성공 처리하여 다음 화면으로 이동
        // ---------------------------------------------------------------
        Toast.makeText(context, "[테스트] 백엔드 연결 없이 진행합니다.", Toast.LENGTH_SHORT).show()
        moveToGenreFragment() // 바로 이동
        // ---------------------------------------------------------------


        /* // ▼▼▼ [백엔드 연결 시 주석 해제할 부분] ▼▼▼
        // 위쪽의 [임시 코드] 2줄을 지우고, 아래 코드의 주석(/* */)을 푸세요.

        RetrofitClient.api.sendSelectedArtists(requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    Log.d("API_SUCCESS", "메시지: ${baseResponse?.message}")

                    // 성공 시 화면 이동
                    moveToGenreFragment()
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

    /* --- 장르 프래그먼트로 이동하느 함수 --- */
    private fun moveToGenreFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.onboarding_fragment_container, GenreFragment())
            .addToBackStack(null)
            .commit()
    }


    /* --- 버튼 가시성 업데이트 --- */
    private fun updateButtonVisibility(count: Int) {
        if (count == 3) {
            // 버튼이 안 보이는 상태라면 보이게 처리 + 애니메이션
            if (binding.nextButton.visibility != View.VISIBLE) {
                // apply 스코프 함수로 여러 뷰를 묶어서 처리
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
            // 버튼 숨김
            binding.run {
                nextButton.visibility = View.GONE
                nextEllipse.visibility = View.GONE
                initButton.visibility = View.GONE
            }
        }
    }


    /* --- 뷰 바인딩 메모리 누수 방지 --- */
    // 프래그먼트는 뷰보다 수명이 길어서, 뷰가 파괴될 때 바인딩 참조도 끊어줘야 함
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

