package com.mobile.soundscape.evaluation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentEvaluationStep1Binding

class EvaluationStep1Fragment : Fragment(R.layout.fragment_evaluation_step1) {
    private val viewModel: EvaluationViewModel by activityViewModels()
    private var _binding: FragmentEvaluationStep1Binding? = null
    private val binding get() = _binding!!

    private val stepLayouts by lazy {
        listOf(
            binding.layoutStep1,
            binding.layoutStep2,
            binding.layoutStep3,
            binding.layoutStep4,
            binding.layoutStep5
        )
    }

    private var currentScore = 4
    private var currentSelectedIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEvaluationStep1Binding.bind(view)

        setupSlider()

        // 1. 초기 인덱스 결정 (처음 진입 시 viewModel.rating이 0이므로 index 0으로 설정)
        // 만약 초기값을 -1 등으로 설정했다면 그에 맞춰 변경 가능합니다.
        val initialIndex = if (viewModel.rating == 0 || viewModel.rating == 4) 0 else (4 - viewModel.rating).coerceIn(0, 4)

        // 2. UI 상태(텍스트 투명도) 즉시 반영
        updateUI(initialIndex)

        // 3. [핵심] 레이아웃이 완전히 그려진 후 핸들 위치를 잡습니다.
        binding.viewSliderTrack.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val track = binding.viewSliderTrack
                val handle = binding.ivSliderHandle

                if (track.height > 0) {
                    // 매우 좋음(0)일 때 targetY는 0이 되어 가장 상단에 위치합니다.
                    val targetY = track.height.toFloat() * (initialIndex.toFloat() / 4f)

                    // 정중앙 정렬을 위해 track.top 기준 좌표 설정
                    handle.y = track.top + targetY - (handle.height / 2)

                    Log.d("EVAL_SLIDER", "초기화 완료: 점수 ${4 - initialIndex}점, Y좌표 ${handle.y}")

                    // 리스너 제거 (한 번만 실행)
                    binding.viewSliderTrack.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

        binding.nextButton.setOnClickListener {
            Log.d("EVAL_SLIDER", "최종 선택 점수: $currentScore 점 (index: $currentSelectedIndex)")
            viewModel.rating = currentScore

            val targetFragment = if (currentScore >= 3) {
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

            val trackTop = track.top.toFloat()
            val trackHeight = track.height.toFloat()

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
                    Log.d("EVAL_SLIDER", "터치 종료: 현재 ${4 - index}점 선택 중")
                }
            }
            true
        }
    }

    private fun updateUI(selectedIndex: Int) {
        currentSelectedIndex = selectedIndex
        currentScore = 4 - selectedIndex // 인덱스 0 -> 4점, 인덱스 4 -> 0점

        stepLayouts.forEachIndexed { index, layout ->
            layout.alpha = if (index == selectedIndex) 1.0f else 0.3f
        }
    }

    private fun snapToStep(index: Int) {
        val track = binding.viewSliderTrack
        val handle = binding.ivSliderHandle
        val targetY = track.height.toFloat() * (index.toFloat() / 4f)

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