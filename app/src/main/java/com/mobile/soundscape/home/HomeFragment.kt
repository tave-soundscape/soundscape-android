package com.mobile.soundscape.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentHomeBinding
import com.mobile.soundscape.evaluation.EvaluationPopupDialog

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 2. View Binding으로 초기화 및 루트 뷰 반환
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root //  binding 객체의 루트 뷰 반환
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 테스트용: 실행하자마자 "평가 필요함" 상태로 강제 설정(서버 연결 전이니깐)
        val sharedPref = requireContext().getSharedPreferences("SoundscapePrefs", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("needs_evaluation", true).apply()

        // 홈화면 구슬 움직이는 animation
        com.bumptech.glide.Glide.with(this)
            .load(R.drawable.orb_animation)
            .into(binding.centerButton)

        // 시작하기 버튼 누르면 추천 받기 시작
        binding.startButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_recPlaceFragment)
        }

        checkAndShowEvaluationPopup()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkAndShowEvaluationPopup() {
        val sharedPref = requireContext().getSharedPreferences("SoundscapePrefs", android.content.Context.MODE_PRIVATE)
        val needsEvaluation = sharedPref.getBoolean("needs_evaluation", false)

        if (needsEvaluation) {
            // 2~3초 뒤에 실행되도록 postDelayed 사용
            view?.postDelayed({
                // 프래그먼트가 아직 화면에 붙어 있는지(isAdded) 확인하는 것이 안전합니다.
                if (isAdded) {
                    val dialog = EvaluationPopupDialog()
                    dialog.show(parentFragmentManager, "EvaluationPopup")
                }
            }, 3000) // 3000ms = 3초
        }
    }
}