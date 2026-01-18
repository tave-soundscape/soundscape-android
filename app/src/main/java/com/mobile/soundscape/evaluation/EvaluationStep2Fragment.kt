package com.mobile.soundscape.evaluation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentEvaluationStep2Binding

class EvaluationStep2Fragment : Fragment(R.layout.fragment_evaluation_step2) {

    private val viewModel: EvaluationViewModel by activityViewModels()
    private var _binding: FragmentEvaluationStep2Binding? = null
    private val binding get() = _binding!!

    // 선택된 이유들을 저장할 리스트
    private val selectedReasons = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvaluationStep2Binding.bind(view)
        
        setupReasonButtons()
        setupNavigationButtons()
    }

    private fun setupReasonButtons() {
        // 버튼 리스트 생성
        val buttons = listOf(
            binding.btnReason1,
            binding.btnReason2,
            binding.btnReason3,
            binding.btnReason4
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                // 1. 선택 상태 반전 (isSelected가 true면 drawable selector가 작동함)
                button.isSelected = !button.isSelected

                // 2. 선택된 텍스트 수집/제거
                val reasonText = button.text.toString()
                if (button.isSelected) {
                    selectedReasons.add(reasonText)
                } else {
                    selectedReasons.remove(reasonText)
                }

                // 3. 최소 하나 이상 선택되어야 '다음' 버튼 활성화
                updateNextButtonState()
            }
        }
    }

    private fun updateNextButtonState() {
        // 하나라도 선택되어 있으면 다음 버튼을 더 진하게 하거나 활성화
        if (selectedReasons.isNotEmpty()) {
            binding.nextButton.alpha = 1.0f
            binding.nextButton.isEnabled = true
        } else {
            binding.nextButton.alpha = 0.5f
            // binding.nextButton.isEnabled = false // 기획에 따라 결정
        }
    }

    private fun setupNavigationButtons() {
        // 이전 버튼: Step 1로 돌아가기
        binding.prevButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 다음 버튼: Step 3(최종 완료)으로 이동
        binding.nextButton.setOnClickListener {
            // 1. 선택된 이유들을 하나의 문자열로 합칩니다. (예: "이유1, 이유2")
            val reasonString = selectedReasons.joinToString(separator = ", ")

            // 2. ViewModel의 String 타입 변수에 저장합니다.
            viewModel.dislikeReason = reasonString

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EvaluationStep3Fragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}