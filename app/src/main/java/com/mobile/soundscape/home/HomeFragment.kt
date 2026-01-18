package com.mobile.soundscape.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 중앙 구슬 애니메이션 재생
        Glide.with(this)
            .load(R.drawable.orb_animation)
            .into(binding.centerButton)

        // 버튼 클릭 이벤트 설정
        setupButtons()
    }

    private fun setupButtons() {
        binding.apply {
            // 시작하기 버튼 -> 추천 받기 화면(recPlaceFragment)으로 이동
            startButton.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_recPlaceFragment)
            }

            // "최근몰입" 탭 -> 히스토리 화면(HomeHistoryFragment)으로 이동
            btnRecent.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_homeHistoryFragment)
            }

            // "추천받기" 탭 -> 현재 화면이므로 아무 동작 안 함
            btnRecommend.setOnClickListener {
                // 이미 이곳에 있음
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}