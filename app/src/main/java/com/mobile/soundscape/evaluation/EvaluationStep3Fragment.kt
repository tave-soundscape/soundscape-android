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
            selectCard(it, "calm", true)
        }

        binding.btnEnergetic.setOnClickListener {
            selectCard(it, "energetic", true)
        }
    }

    private fun setupStep32Listeners() {
        binding.btnLyricsOn.setOnClickListener {
            selectCard(it, "lyrics_on", false)
        }

        binding.btnLyricsOff.setOnClickListener {
            selectCard(it, "lyrics_off", false)
        }
    }

    private fun selectCard(view: View, value: String, isMood: Boolean) {
        // 1. 시각적 피드백: 클릭한 버튼을 '선택됨' 상태로 변경
        view.isSelected = true

        // 2. 데이터 저장
        if (isMood) viewModel.preferredMood = value else viewModel.lyricsPreference = value

        // 3. 0.2초 뒤에 다음 단계로 이동 (색상 변화를 볼 시간을 줌)
        view.postDelayed({
            if (isMood) showStep32() else navigateToStep4()
        }, 200)
    }

    //step3_1 -> step3-2
    private fun showStep32() {

        // 카드 레이아웃 교체 (Visibility)
        binding.layoutStep31Cards.visibility = View.GONE
        binding.layoutStep32Cards.visibility = View.VISIBLE

        // 애니메이션 추가 시 더 자연스러움
        binding.layoutStep32Cards.alpha = 0f
        binding.layoutStep32Cards.animate().alpha(1f).setDuration(300).start()
    }

    //step4 로 이동
    private fun navigateToStep4() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EvaluationStep4Fragment())
            .addToBackStack(null)
            .commit()
    }
}