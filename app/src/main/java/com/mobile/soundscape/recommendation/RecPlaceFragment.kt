package com.mobile.soundscape.recommendation

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentRecPlaceBinding
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels

data class Place(
    val id: String,
    val name: String,
    val englishName: String,
    val iconColorId: Int,
    val iconDrawableId: Int,
    val wrapperId: Int
)

class RecPlaceFragment : Fragment() {

    private var _binding: FragmentRecPlaceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationViewModel by activityViewModels()

    private lateinit var allButtons: List<View>
    private var selectedButtonWrapper: View? = null

    private val allPlaceData = listOf(
        Place("p0", "집/실내", "home", R.color.btn_pink, R.drawable.place_icon0, R.id.center_button),
        Place("p1", "카페", "cafe", R.color.btn_orange, R.drawable.place_icon1, R.id.btn1_wrapper),
        Place("p2", "코워킹", "co-working", R.color.btn_blue, R.drawable.place_icon2, R.id.btn2_wrapper),
        Place("p3", "헬스장", "gym", R.color.btn_purple, R.drawable.place_icon3, R.id.btn3_wrapper),
        Place("p4", "도서관", "library", R.color.btn_yellow, R.drawable.place_icon4, R.id.btn4_wrapper),
        Place("p5", "이동중", "moving", R.color.btn_red, R.drawable.place_icon5, R.id.btn5_wrapper),
        Place("p6", "공원", "park", R.color.btn_green, R.drawable.place_icon6, R.id.btn6_wrapper)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGroupWrapper.post {

            val minSide = Math.min(binding.buttonGroupWrapper.width, binding.buttonGroupWrapper.height)

            // 반지름을 전체 크기의 32% 정도로 설정 (가장 안전한 비율)
            val safeRadius = (minSide * 0.32).toInt()

            val wrappers = listOf(
                binding.btn1Wrapper, binding.btn2Wrapper, binding.btn3Wrapper,
                binding.btn4Wrapper, binding.btn5Wrapper, binding.btn6Wrapper
            )

            wrappers.forEach { wrapper ->
                val params = wrapper.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                params.circleRadius = safeRadius
                wrapper.layoutParams = params
            }
        }

        // 모든 버튼을 목록에 저장
        allButtons = listOf(
            binding.centerButton,
            binding.btn1Wrapper,
            binding.btn2Wrapper,
            binding.btn3Wrapper,
            binding.btn4Wrapper,
            binding.btn5Wrapper,
            binding.btn6Wrapper
        )

        // 초기 상태: 버튼과 ellipse 안 보이게
        binding.ellipse.isVisible = false
        binding.nextBtn.isVisible = false
        binding.nextBtn.isEnabled = false

        // 모든 버튼에 클릭 리스너 설정
        allButtons.forEach { buttonWrapper ->
            buttonWrapper.setOnClickListener {
                handlePlaceSelection(buttonWrapper)
            }
        }

        binding.nextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_recPlaceFragment_to_recDecibelFragment)
        }
    }

    private fun handlePlaceSelection(newlySelectedWrapper: View) {
        val selectedData = allPlaceData.find { it.wrapperId == newlySelectedWrapper.id }
        if (selectedData == null) return

        // 1. 이전에 선택된 버튼의 스타일 초기화
        if (selectedButtonWrapper != null) {
            val previousData = allPlaceData.find { it.wrapperId == selectedButtonWrapper!!.id }
            if (previousData != null) {
                selectedButtonWrapper!!.setBackgroundResource(R.drawable.button_default_background)
                val previousIcon = selectedButtonWrapper!!.findViewById<ImageView>(getIconIdForWrapper(previousData.wrapperId))
                previousIcon?.imageTintList = null
            }
        }

        // 2. Toggle Logic (선택 상태 변경)
        if (selectedButtonWrapper == newlySelectedWrapper) {
            selectedButtonWrapper = null
        } else {
            val selectedColorInt = ContextCompat.getColor(requireContext(), selectedData.iconColorId)

            // RecGoalFragment와 동일한 진한 농도
            val colors = intArrayOf(
                Color.TRANSPARENT,
                Color.argb(0x40, Color.red(selectedColorInt), Color.green(selectedColorInt), Color.blue(selectedColorInt)), // 0x10 -> 0x40 변경
                selectedColorInt
            )

            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors
            ).apply {
                shape = GradientDrawable.OVAL
                gradientType = GradientDrawable.RADIAL_GRADIENT
                // RecGoalFragment와 동일한 크기 (반지름 1.0배)
                gradientRadius = (newlySelectedWrapper.width.toFloat().takeIf { it > 0 } ?: 100f * resources.displayMetrics.density) * 0.7f // 0.5f -> 1.0f 변경
            }

            newlySelectedWrapper.background = gradientDrawable

            val iconView = newlySelectedWrapper.findViewById<ImageView>(getIconIdForWrapper(selectedData.wrapperId))
            iconView?.imageTintList = ColorStateList.valueOf(selectedColorInt)

            selectedButtonWrapper = newlySelectedWrapper

            // 뷰모델에 데이터 저장
            viewModel.place = selectedData.name
            viewModel.englishPlace = selectedData.englishName
            viewModel.checkData()
        }

        // 3. 주변 버튼 뿌옇게
        val isSelected = selectedButtonWrapper != null
        allButtons.forEach { buttonWrapper ->
            val textViewId = getTextViewIdForWrapper(buttonWrapper.id)
            val textView = view?.findViewById<TextView>(textViewId)

            if (!isSelected) {
                buttonWrapper.alpha = 1.0f
                textView?.alpha = 1.0f
            } else {
                val targetAlpha = if (buttonWrapper == selectedButtonWrapper) 1.0f else 0.4f
                buttonWrapper.alpha = targetAlpha
                textView?.alpha = targetAlpha
            }
        }

        // 4.다음 버튼 활성화
        binding.ellipse.isVisible = isSelected
        binding.nextBtn.isVisible = isSelected
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
            else -> throw IllegalArgumentException("Unknown wrapper ID for text: $wrapperId")
        }
    }

    // 메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}