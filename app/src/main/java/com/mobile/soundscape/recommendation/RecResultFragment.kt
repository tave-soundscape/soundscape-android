package com.mobile.soundscape.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentRecResultBinding
import com.mobile.soundscape.result.PlaylistResultActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecResultFragment : Fragment() {

    private var _binding: FragmentRecResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 5초 딜레이 시작 (Coroutines 사용)
        // (viewLifecycleOwner를 사용해야 화면이 꺼지면 타이머도 안전하게 종료됨)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(5000) // 5000ms = 5초 대기

            updateUIForCompletion()
        }

        // "보러가기" 버튼을 누르면 -> 플레이리스트로 이동
        binding.nextButton.setOnClickListener {
            val intent = android.content.Intent(requireContext(), PlaylistResultActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun updateUIForCompletion() {
        // 뷰 바인딩이 유효한지 확인 (혹시 모를 크래시 방지)
        if (_binding == null) return

        binding.apply {
            // 1. 텍스트 변경
            tvSubtitle.text = "오늘의 몰입 플레이리스트가\n완성됐어요"

            // 2. "잠시만 기다려주세요" 텍스트 숨기기
            tvSubtitle2.visibility = View.GONE

            // 3. 버튼과 타원(ellipse) 보이기
            nextButton.visibility = View.VISIBLE
            ellipse.visibility = View.VISIBLE

            // 보러가기 버튼 애니메이션 주기
            nextButton.alpha = 0f
            nextButton.animate().alpha(1f).setDuration(500).start()

            ellipse.alpha = 0f
            ellipse.animate().alpha(1f).setDuration(1000).start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}