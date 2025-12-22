package com.mobile.soundscape.recommendation

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentRecGoalBinding
import com.mobile.soundscape.databinding.WidgetProgressBarBinding

data class Goal(
    val id: String,
    val name: String,
    val iconColorId: Int, // R.color.btn_pink 등
    val iconDrawableId: Int, // R.drawable.goal_icon0 등
    val wrapperId: Int      // R.id.center_button, R.id.btn1_wrapper 등 ConstraintLayout ID
)
class RecGoalFragment : Fragment() {

    private var _binding: FragmentRecGoalBinding? = null
    private val binding get() = _binding!!

    private lateinit var allButtons: List<View>
    private var selectedButtonWrapper: View? = null // 현재 선택된 버튼 저장

    //  3. 목표 데이터 정의 (XML ID와 텍스트 레이블에 맞춤)
    private val allGoalData = listOf(
        Goal("g0", "수면", R.color.btn_pink, R.drawable.goal_icon0, R.id.center_button),
        Goal("g1", "집중", R.color.btn_orange, R.drawable.goal_icon1, R.id.btn1_wrapper),
        Goal("g2", "위로", R.color.btn_blue, R.drawable.goal_icon2, R.id.btn2_wrapper),
        Goal("g3", "활력", R.color.btn_purple, R.drawable.goal_icon3, R.id.btn3_wrapper),
        Goal("g4", "안정", R.color.btn_yellow, R.drawable.goal_icon4, R.id.btn4_wrapper),
        Goal("g5", "분노", R.color.btn_red, R.drawable.goal_icon5, R.id.btn5_wrapper),
        Goal("g6", "휴식", R.color.btn_green, R.drawable.goal_icon6, R.id.btn6_wrapper),
        Goal("g7", "미선택", R.color.btn_empty, R.drawable.goal_icon7, R.id.btn7_wrapper) // 미선택 버튼 추가
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. 모든 버튼을 목록에 저장 (XML의 btn1 ~ btn6 ID 사용 가정)
        allButtons = listOf(
            binding.centerButton,
            binding.btn1Wrapper,
            binding.btn2Wrapper,
            binding.btn3Wrapper,
            binding.btn4Wrapper,
            binding.btn5Wrapper,
            binding.btn6Wrapper,
            binding.btn7Wrapper
        )

        //  초기 상태 설정
        binding.ellipse.isVisible = false
        binding.nextBtn.isVisible = false
        binding.nextBtn.isEnabled = false


        //  모든 버튼에 클릭 리스너 설정
        allButtons.forEach { buttonWrapper ->
            buttonWrapper.setOnClickListener {
                handleGoalSelection(buttonWrapper)
            }
        }

        //  다음 버튼 클릭 (최종 결과 화면으로 이동)
        binding.nextBtn.setOnClickListener {
            // RecResultFragment로 이동하는 Navigation Action 실행
            findNavController().navigate(R.id.action_recGoalFragment_to_recResultFragment)
        }

        // TODO: loadRadialButtons 호출 (목표 데이터로 Custom View 채우기)
    }


    private fun handleGoalSelection(newlySelectedWrapper: View) {
        val selectedData = allGoalData.find { it.wrapperId == newlySelectedWrapper.id }
        if (selectedData == null) return

        // 1. 이전에 선택된 버튼의 스타일 초기화
        if (selectedButtonWrapper != null) {
            val previousData = allGoalData.find { it.wrapperId == selectedButtonWrapper!!.id }
            if (previousData != null) {
                selectedButtonWrapper!!.setBackgroundResource(R.drawable.button_default_background)
                val previousIcon = selectedButtonWrapper!!.findViewById<ImageView>(getIconIdForWrapper(previousData.wrapperId))
                previousIcon?.imageTintList = null

                // 텍스트도 초기화
                val previousTextViewId = getTextViewIdForWrapper(previousData.wrapperId)
                view?.findViewById<TextView>(previousTextViewId)?.alpha = 1.0f
            }
        }

        // 2. 선택 상태 토글 로직
        if (selectedButtonWrapper == newlySelectedWrapper) {
            selectedButtonWrapper = null
        } else {
            // 새로운 버튼 선택
            val selectedColorInt = ContextCompat.getColor(requireContext(), selectedData.iconColorId)

            // 3. 그라데이션
            val outerEdgeColor = Color.argb(0xFF, Color.red(selectedColorInt), Color.green(selectedColorInt), Color.blue(selectedColorInt))
            val newAlpha = 0x40

            val colors = intArrayOf(
                Color.TRANSPARENT,
                Color.argb(newAlpha, Color.red(selectedColorInt), Color.green(selectedColorInt), Color.blue(selectedColorInt)),
                outerEdgeColor
            )

            val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors)
            gradientDrawable.shape = GradientDrawable.OVAL
            gradientDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT

            val buttonSizePx = newlySelectedWrapper.width.toFloat().takeIf { it > 0 } ?: 100f * resources.displayMetrics.density
            gradientDrawable.gradientRadius = buttonSizePx * 1.0f // 반지름 조정 로직 유지

            newlySelectedWrapper.background = gradientDrawable

            // 4. 아이콘 색상 적용
            val iconView = newlySelectedWrapper.findViewById<ImageView>(getIconIdForWrapper(selectedData.wrapperId))
            iconView?.imageTintList = ColorStateList.valueOf(selectedColorInt)

            selectedButtonWrapper = newlySelectedWrapper
        }

        // 5. 주변 버튼 뿌옇게
        val isSelected = selectedButtonWrapper != null
        allButtons.forEach { buttonWrapper ->
            val textViewId = getTextViewIdForWrapper(buttonWrapper.id)
            val textView = view?.findViewById<TextView>(textViewId)

            if (selectedButtonWrapper == null) {
                buttonWrapper.alpha = 1.0f
                textView?.alpha = 1.0f
            } else {
                val targetAlpha = if (buttonWrapper == selectedButtonWrapper) 1.0f else 0.4f
                buttonWrapper.alpha = targetAlpha
                textView?.alpha = targetAlpha
            }
        }

        // 6. 다음 버튼 활성화
        binding.ellipse.isVisible = isSelected
        binding.nextBtn.isVisible = true
        binding.nextBtn.isEnabled = isSelected
        binding.nextBtn.alpha = if (isSelected) 1.0f else 0.5f
    }

    private fun getIconIdForWrapper(wrapperId: Int): Int {
        return when (wrapperId) {
            R.id.center_button -> R.id.btn0_icon
            R.id.btn1_wrapper -> R.id.btn1_icon
            R.id.btn2_wrapper -> R.id.btn2_icon
            R.id.btn3_wrapper -> R.id.btn3_icon
            R.id.btn4_wrapper -> R.id.btn4_icon
            R.id.btn5_wrapper -> R.id.btn5_icon
            R.id.btn6_wrapper -> R.id.btn6_icon
            R.id.btn7_wrapper -> R.id.btn7_icon // btn7 추가
            else -> throw IllegalArgumentException("Unknown wrapper ID: $wrapperId")
        }
    }

    private fun getTextViewIdForWrapper(wrapperId: Int): Int {
        return when (wrapperId) {
            R.id.center_button -> R.id.txt0
            R.id.btn1_wrapper -> R.id.txt1
            R.id.btn2_wrapper -> R.id.txt2
            R.id.btn3_wrapper -> R.id.txt3
            R.id.btn4_wrapper -> R.id.txt4
            R.id.btn5_wrapper -> R.id.txt5
            R.id.btn6_wrapper -> R.id.txt6
            R.id.btn7_wrapper -> R.id.txt7 // txt7 추가 (XML에 ID가 없으면 에러 발생)
            else -> throw IllegalArgumentException("Unknown wrapper ID for text: $wrapperId")
        }
    }


    //  메모리 누수 방지 (필수)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}