package com.mobile.soundscape.onboarding

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentSetnameBinding


class SetnameFragment : Fragment() {

    // 실제 데이터 저장소 (비울 수 있어야 하니까 Nullable)
    private var _binding: FragmentSetnameBinding? = null

    // 접근용 껍데기 (Null 체크 없이 편하게 쓰려고 만듦)
    // get()을 쓸 때마다 _binding을 가져옴. !!로 null이 아님을 보장.
    private val binding get() = _binding!!


    // 색상 정의 (파랑: 성공 / 빨강: 실패 / 회색: 기본)
    private val colorSuccess = Color.parseColor("#4511FF") // Royal Blue
    private val colorError = Color.parseColor("#F6443A")   // Red
    private val colorDefault = Color.parseColor("#4A494C")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetnameBinding.inflate(inflater, container, false)
        return binding.root    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 텍스트 입력 감지 리스너 설정
        binding.getNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()

                // --- 조건 검자 확인 ---

                // 조건 1. 특수문자 불가 (한글, 영문, 숫자만 허용)
                // 정규식: ^[0-9a-zA-Z가-힣]*$ -> 특수문자가 없으면 true
                val isCharValid = input.matches(Regex("^[0-9a-zA-Z가-힣]*$")) && input.isNotEmpty()


                // 조건 2. 길이 검사 (한글이면 1~10자, 영어면 2~20자)
                val hasKorean = input.matches(Regex(".*[가-힣]+.*"))
                val len = input.length
                val isLengthValid = if (input.isEmpty()) {
                    false // 비어있으면 길이 조건 실패
                } else if (hasKorean) {
                    len in 1..10
                } else {
                    len in 2..20
                }
                

                // --- UI 업데이트 (색상 변경) ---

                // 1) 특수문자 텍스트뷰 업데이트
                updateConditionUI(binding.nameRuleCharacter, isCharValid, input.isEmpty())

                // 2) 길이 텍스트뷰 업데이트
                updateConditionUI(binding.nameRuleLength, isLengthValid, input.isEmpty())


                // --- 버튼 및 타원 이미지 표시 여부 ---
                // 두 조건이 모두 '참'일 때만 버튼을 보여줌
                if (isCharValid && isLengthValid) {
                    binding.nextButton.visibility = View.VISIBLE
                    binding.ellipse.visibility = View.VISIBLE

                    // 애니메이션
                    binding.nextButton.alpha = 0f
                    binding.nextButton.animate().alpha(1f).setDuration(300).start()
                    binding.ellipse.alpha = 0f
                    binding.ellipse.animate().alpha(1f).setDuration(300).start()

                } else {
                    binding.nextButton.visibility = View.GONE
                    binding.ellipse.visibility = View.GONE
                }
            }
        })

        // '다음으로' 버튼 누르면 아티스트 고르는 프래그먼트로 이동
        binding.nextButton.setOnClickListener {
            val nextFragment = ArtistFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.onboarding_fragment_container, nextFragment)
                .addToBackStack(null)
                .commit()
        }
    }


    // 텍스트뷰의 색상과 아이콘 색상을 변경하는 함수
    private fun updateConditionUI(textView: TextView, isValid: Boolean, isEmpty: Boolean) {
        val color = when {
            isEmpty -> colorDefault   // 입력 없으면 회색
            isValid -> colorSuccess   // 조건 맞으면 파란색
            else -> colorError        // 조건 틀리면 빨간색
        }

        // 글자 색상 변경
        textView.setTextColor(color)

        // 왼쪽 체크 아이콘 색상 변경 (Tint)
        textView.compoundDrawablesRelative[0]?.setTint(color)
    }
}