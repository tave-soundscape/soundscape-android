package com.mobile.soundscape.evaluation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentEvaluationCompleteBinding

class EvaluationCompleteFragment : Fragment(R.layout.fragment_evaluation_complete) {

    private var _binding: FragmentEvaluationCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvaluationCompleteBinding.bind(view)

        // 화면 진입 시 애니메이션 실행
        startCompletionAnimation()

        // 닫기 버튼 클릭 시 액티비티 종료 (홈으로 복귀)
        binding.nextButton.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun startCompletionAnimation() {
        if (_binding == null) return

        binding.apply {
            // 1. 초기 상태 설정 (혹시 XML에서 놓쳤을 경우 대비)
            nextButton.visibility = View.VISIBLE
            ellipse.visibility = View.VISIBLE

            nextButton.alpha = 0f
            ellipse.alpha = 0f

            // 위치 애니메이션 효과를 주고 싶다면 translationY 사용 가능
            nextButton.translationY = 50f
            ellipse.translationY = 200f

            // 2. 버튼 애니메이션 (0.5초 동안 서서히 나타남)
            nextButton.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(300) // 약간의 시차를 둠
                .start()

            // 3. 타원(ellipse) 애니메이션 (1초 동안 아래서 위로 올라옴)
            ellipse.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}