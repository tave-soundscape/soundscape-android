package com.mobile.soundscape.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobile.soundscape.databinding.FragmentGenreBinding
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.MainActivity
import com.mobile.soundscape.data.GenreDataFix
import kotlin.jvm.java

class GenreFragment : Fragment() {

    // View Binding
    private var _binding: FragmentGenreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    // 어댑터
    private lateinit var adapter: GenreAdapter
    // 선택된 장르를 기억하는 전역 저장소 (이름을 키로 사용)
    private val selectedGenresMap = mutableMapOf<String, GenreData>()
    // 전체 장르 원본 데이터 (GenreDataFix에서 불러옴)
    private lateinit var originalGenreList: List<GenreData>

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

        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = GenreAdapter(originalGenreList) { genre, position ->
            handleGenreClick(genre, position)
        }

        binding.rvGenreList.apply {
            this.adapter = this@GenreFragment.adapter
            // 3열 그리드 (동그라미 카드가 3개씩 나열됨)
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    // 클릭 처리 로직 (Map 사용)
    private fun handleGenreClick(genre: GenreData, position: Int) {
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
                // Adapter 갱신 없이 리턴 (UI 변화 없음)
                return
            }
        }

        // 어댑터에게 해당 아이템이 변경되었음을 알림 (테두리 갱신)
        adapter.notifyItemChanged(position)

        // 버튼 상태 업데이트
        updateButtonVisibility()
    }

    private fun setupButtons() {
        // 온보딩 완료 버튼 
        binding.nextButton.setOnClickListener {
            if(selectedGenresMap.size == 3) {
                // 선택한 장르 이름들만 추출하여 뷰모델에 저장
                val genreNameList = selectedGenresMap.keys.toMutableList()
                viewModel.selectedGenres = genreNameList

                // 서버 전송 요청
                viewModel.submitOnboarding()
            } else {
                Toast.makeText(context, "3가지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 초기화 버튼
        binding.initButton.setOnClickListener {
            selectedGenresMap.clear() // 저장소 초기화
            adapter.clearSelection()  // UI 초기화
            updateButtonVisibility()  // 버튼 숨김
        }
    }

    private fun observeViewModel() {
        viewModel.onboardingResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                // 성공 신호 받음 -> 화면 이동
                Toast.makeText(context, "환영합니다! ${viewModel.nickname}님", Toast.LENGTH_SHORT).show()
                moveToNextActivity()
            } else {
                // 실패 신호 받음 -> 토스트 띄우기
                Toast.makeText(context, "서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToNextActivity() {
        // 온보딩 끝나면 메인 액티비티(또는 PlaytestActivity)로 이동 및 스택 초기화
        // flag를 써서 뒤로가기 눌러도 온보딩으로 못 돌아오게 막음
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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