package com.mobile.soundscape.evaluation

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentEvaluationStep4Binding

class EvaluationStep4Fragment : Fragment(R.layout.fragment_evaluation_step4) {

    private val viewModel: EvaluationViewModel by activityViewModels()
    private var _binding: FragmentEvaluationStep4Binding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvaluationStep4Binding.bind(view)

        // 초기 버튼 상태 설정 (텍스트 없으면 비활성화할지 여부 결정)
        binding.nextButton.isEnabled = false
        binding.nextButton.alpha = 0.5f

        // 텍스트 입력 감지 로직
        binding.etOpinion.addTextChangedListener {
            val hasText = it?.isNotEmpty() ?: false
            binding.nextButton.isEnabled = hasText
            binding.nextButton.alpha = if (hasText) 1.0f else 0.5f
        }

        binding.prevButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.nextButton.setOnClickListener {
            val userOpinion = binding.etOpinion.text.toString()
            viewModel.opinion = userOpinion

            navigateToStep5()
        }
    }

    private fun navigateToStep5() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EvaluationStep5Fragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}