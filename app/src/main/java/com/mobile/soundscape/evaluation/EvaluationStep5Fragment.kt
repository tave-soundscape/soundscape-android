package com.mobile.soundscape.evaluation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentEvaluationStep5Binding

class EvaluationStep5Fragment : Fragment(R.layout.fragment_evaluation_step5) {

    private val viewModel: EvaluationViewModel by activityViewModels()
    private var _binding: FragmentEvaluationStep5Binding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvaluationStep5Binding.bind(view)

        // "예" 버튼 클릭 시
        binding.btnYes.setOnClickListener {
            saveFinalDataAndFinish(true)
        }

        // "아니오" 버튼 클릭 시
        binding.btnNo.setOnClickListener {
            saveFinalDataAndFinish(false)
        }
    }

    private fun saveFinalDataAndFinish(willReuse: Boolean) {
        viewModel.reuseIntention = willReuse
        // ViewModel의 제출 함수 호출 (로그 출력 및 서버 전송 준비)
        viewModel.submitEvaluation()

        navigateToComplete()
    }

    private fun navigateToComplete() {
        // 데이터 수집은 끝났으므로, 마지막 '감사합니다' 화면(Step 6)으로 이동
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EvaluationCompleteFragment())
            .commit() // 마지막이므로 BackStack에 추가하지 않음 (뒤로가기 방지)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}