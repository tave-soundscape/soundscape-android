package com.mobile.soundscape.onboarding

import android.R.attr.visible
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentSetnameBinding
import androidx.fragment.app.activityViewModels
import com.mobile.soundscape.api.apis.MypageApi
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.MypageNameRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SetnameFragment : Fragment() {

    // 실제 데이터 저장소 (비울 수 있어야 하니까 Nullable)
    private var _binding: FragmentSetnameBinding? = null

    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by activityViewModels()


    // 색상 정의 (파랑: 성공 / 빨강: 실패 / 회색: 기본)
    private val colorSuccess = Color.parseColor("#34C759")
    private val colorError = Color.parseColor("#ED433A")
    private val colorDefault = Color.parseColor("#4A494C")
    private var currentMode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetnameBinding.inflate(inflater, container, false)
        return binding.root    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 모드 확인 (Bundle에서 데이터 꺼내기)
        currentMode = arguments?.getString("mode")

        // 2. 수정 모드일 경우 UI 변경
        if (currentMode == "edit") {
            binding.nextButton.text = "이름 수정하기"      // 버튼 텍스트 변경
            binding.tvTitle.text = "이름 수정"
            binding.tvQuestion.text = "어떤 이름으로 수정할까요?"
            binding.layoutProgress.visibility = View.GONE
            binding.editmodeProgress.visibility = View.VISIBLE
        }

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


                // --- 아이콘 (X / Check) 표시 로직 추가 ---
                if (input.isEmpty()) {
                    // 입력이 없으면 아이콘 모두 숨김
                    binding.validX.visibility = View.GONE
                    binding.validCheck.visibility = View.GONE
                } else if (isCharValid && isLengthValid) {
                    // 둘 다 성공하면 Check 표시
                    binding.validX.visibility = View.GONE
                    binding.validCheck.visibility = View.VISIBLE
                } else {
                    // 하나라도 실패하면 X 표시
                    binding.validX.visibility = View.VISIBLE
                    binding.validCheck.visibility = View.GONE
                }

                // --- 테두리(Stroke) 처리 ---
                // 글자가 하나라도 있으면 테두리 생성이므로 input.isNotEmpty()만 체크
                val background = binding.getNameInput.background as? android.graphics.drawable.GradientDrawable
                val strokeWidthPx = (2 * resources.displayMetrics.density).toInt()

                if (input.isNotEmpty()) {
                    background?.setStroke(strokeWidthPx, Color.parseColor("#4A494C"))
                } else {
                    background?.setStroke(0, 0)
                }


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

        // '다음으로' 버튼 누르면 -> 뷰모델에 닉네임 담고
        // -> 아티스트 고르는 프래그먼트로 이동
        binding.nextButton.setOnClickListener {
            val finalNickname = binding.getNameInput.text.toString().trim()
            viewModel.nickname = finalNickname // 뷰모델에 닉네임 저장
            viewModel.updateNickname(requireContext(), finalNickname)

            if(currentMode == "edit") {
                updateNicknameToServer(finalNickname)
            } else {
                // UI 정리
                val background =
                    binding.getNameInput.background as? android.graphics.drawable.GradientDrawable
                background?.setStroke(0, 0)

                // 다음 아티스트 프래그먼트로 이동
                val nextFragment = ArtistFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.onboarding_fragment_container, nextFragment)
                    .addToBackStack(null)
                    .commit()
            }
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

    // 서버로 닉네임 수정 요청 보내기
    private fun updateNicknameToServer(newName: String) {

        RetrofitClient.mypageApi.updateName(MypageNameRequest(newName)).enqueue( object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "이름 변경 실패: ${response.code()}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("SetnameFragment", "Error: ${t.message}")
            }
        })

    }
}