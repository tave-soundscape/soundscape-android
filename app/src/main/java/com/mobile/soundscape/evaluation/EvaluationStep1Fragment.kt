package com.mobile.soundscape.evaluation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentEvaluationStep1Binding

class EvaluationStep1Fragment : Fragment(R.layout.fragment_evaluation_step1) {
    private val viewModel: EvaluationViewModel by activityViewModels()
    private var _binding: FragmentEvaluationStep1Binding? = null
    private val binding get() = _binding!!

    // 평가 단계별 레이아웃 리스트 (위에서부터 순서대로)
    private val stepLayouts by lazy {
        listOf(
            binding.layoutStep1,
            binding.layoutStep2,
            binding.layoutStep3,
            binding.layoutStep4,
            binding.layoutStep5
        )
    }

    private var currentScore = 5 // 기본값: 매우 좋음 (5점)
    private var currentSelectedIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvaluationStep1Binding.bind(view)

        setupSlider()

        // 1. 저장된 점수가 -1(초기상태)이면 0(매우 좋음)을 사용, 아니면 저장된 값 복구
        val savedIndex = if (viewModel.rating == -1) {
            0 // 처음 왔을 때는 매우 좋음 위치
        } else {
            (5 - viewModel.rating).coerceIn(0, 4) // 돌아왔을 때는 이전 점수 위치
        }

        // 2. UI와 점수 변수 동기화
        updateUI(savedIndex)

        // 3. 핸들 위치 이동
        binding.evaluationContainer.post {
            snapToStep(savedIndex)
        }

        binding.nextButton.setOnClickListener {
            // 2. [중요] 이동 직전 현재 점수를 한 번 더 ViewModel에 확정 저장
            viewModel.rating = currentScore

            val targetFragment = if (currentSelectedIndex <= 1) {
                EvaluationStep3Fragment()
            } else {
                EvaluationStep2Fragment()
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlider() {
        binding.evaluationContainer.setOnTouchListener { _, event ->
            val track = binding.viewSliderTrack
            val handle = binding.ivSliderHandle

            // 트랙의 실제 Y 위치값 계산
            val trackTop = track.top.toFloat()
            val trackBottom = track.bottom.toFloat()
            val trackHeight = track.height.toFloat()

            // 터치 좌표가 트랙 범위 내에 오도록 제한
            val clampedY = (event.y - trackTop).coerceIn(0f, trackHeight)

            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    handle.y = trackTop + clampedY - (handle.height / 2)
                    val index = (Math.round((clampedY / trackHeight) * 4)).toInt().coerceIn(0, 4)
                    updateUI(index)
                }
                MotionEvent.ACTION_UP -> {
                    val index = (Math.round((clampedY / trackHeight) * 4)).toInt().coerceIn(0, 4)
                    snapToStep(index)
                }
            }
            true
        }
    }

    private fun updateUI(selectedIndex: Int) {
        currentSelectedIndex = selectedIndex // 인덱스 업데이트
        currentScore = 5 - selectedIndex    // 0이면 5점, 4이면 1점

        // ViewModel에 실시간 반영
        viewModel.rating = currentScore

        // 선택된 단계만 진하게, 나머지는 흐리게 (alpha 조절)
        // stepLayouts: [layoutStep1, layoutStep2, layoutStep3, layoutStep4, layoutStep5]
        stepLayouts.forEachIndexed { index, layout ->
            if (index == selectedIndex) {
                layout.alpha = 1.0f // 선택된 것은 진하게
            } else {
                layout.alpha = 0.3f // 나머지는 흐리게
            }
        }
    }

    private fun snapToStep(index: Int) {
        val track = binding.viewSliderTrack
        val handle = binding.ivSliderHandle

        // 인덱스에 따른 목표 Y 좌표 (0%, 25%, 50%, 75%, 100%)
        val targetY = track.height.toFloat() * (index.toFloat() / 4f)

        // 애니메이션으로 부드럽게 이동
        handle.animate()
            .y(track.top + targetY - (handle.height / 2))
            .setDuration(150)
            .start()

        updateUI(index)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}