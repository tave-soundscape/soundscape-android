package com.mobile.soundscape.evaluation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentEvaluationStep3Binding

class EvaluationStep3Fragment : Fragment(R.layout.fragment_evaluation_step3) {

    private val viewModel: EvaluationViewModel by activityViewModels()
    private var _binding: FragmentEvaluationStep3Binding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvaluationStep3Binding.bind(view)

        setupStep31Listeners()
        setupStep32Listeners()
    }

    //step3_1 차분 vs 에너제틱
    private fun setupStep31Listeners() {
        binding.btnCalm.setOnClickListener {
            viewModel.atmosphere = "calm" // 분위기 저장
            showStep32()
        }

        binding.btnEnergetic.setOnClickListener {
            viewModel.atmosphere = "energetic" // 분위기 저장
            showStep32()
        }
    }

    //step3_2 가사O vs 가사X
    private fun setupStep32Listeners() {
        binding.btnLyricsOn.setOnClickListener {
            viewModel.lyricsPreference = "lyrics_on" // 가사 선호도 저장
            navigateToStep4()
        }

        binding.btnLyricsOff.setOnClickListener {
            viewModel.lyricsPreference = "lyrics_off" // 가사 선호도 저장
            navigateToStep4()
        }
    }

    //step3_1 -> step3-2
    private fun showStep32() {

        // 카드 레이아웃 교체 (Visibility)
        binding.layoutStep31Cards.visibility = View.GONE
        binding.layoutStep32Cards.visibility = View.VISIBLE

        // (선택사항) 애니메이션 추가 시 더 자연스러움
        binding.layoutStep32Cards.alpha = 0f
        binding.layoutStep32Cards.animate().alpha(1f).setDuration(300).start()
    }

    //step4 로 이동
    private fun navigateToStep4() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EvaluationStep4Fragment()) // Step 4로 교체
            .addToBackStack(null) // 사용자가 뒤로가기를 누를 경우를 대비
            .commit()
    }
}