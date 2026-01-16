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

        // selectItem에 "한글 이름"만 전달하도록 수정
        binding.tvOptionPlace.setOnClickListener { selectItem("장소") }
        binding.tvOptionGoal.setOnClickListener { selectItem("목표") }
        binding.tvOptionNoise.setOnClickListener { selectItem("소음") }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun selectItem(name: String) {
        onItemSelected(name) // "장소", "목표", "소음" 중 하나가 전달됨
        dismiss()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}