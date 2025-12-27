package com.mobile.soundscape.evaluation

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.mobile.soundscape.databinding.FragmentEvaluationPopupDialogBinding

class EvaluationPopupDialog : DialogFragment() {

    private var _binding: FragmentEvaluationPopupDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 1. 뷰 바인딩 초기화
        _binding = FragmentEvaluationPopupDialogBinding.inflate(inflater, container, false)

        // 2. 다이얼로그 윈도우 설정 (배경 투명화)
        dialog?.window?.apply {
            // 커스텀 배경(bg_popup_rounded)의 둥근 모서리를 위해 기본 프레임을 투명하게 설정
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 팝업이 뜨는 애니메이션을 넣고 싶다면 여기에 추가 가능
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단 'X' 닫기 버튼
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // '평가하러 가기' 보라색 버튼
        binding.btnGoEvaluation.setOnClickListener {
            // EvaluationActivity로 이동
            val intent = Intent(requireContext(), EvaluationActivity::class.java)
            startActivity(intent)
            // 팝업 닫기
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // 3. 팝업 너비 가로 사이즈 고정 (화면 가로의 약 85% 또는 특정 dp)
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}