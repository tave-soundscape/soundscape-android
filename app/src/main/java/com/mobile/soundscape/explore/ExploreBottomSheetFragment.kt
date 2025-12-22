package com.mobile.soundscape.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobile.soundscape.databinding.LayoutExploreBottomSheetBinding

class ExploreBottomSheetFragment(val onItemSelected: (String) -> Unit) : BottomSheetDialogFragment() {

    private var _binding: LayoutExploreBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutExploreBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 각 항목 클릭 시 메인 프래그먼트로 값 전달 후 닫기
        binding.tvOptionPlace.setOnClickListener { selectItem("장소") }
        binding.tvOptionGoal.setOnClickListener { selectItem("목표") }
        binding.tvOptionNoise.setOnClickListener { selectItem("소음") }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun selectItem(item: String) {
        onItemSelected(item)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}