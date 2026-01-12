package com.mobile.soundscape.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.data.AppDatabase
import com.mobile.soundscape.data.PlaylistHistory
import com.mobile.soundscape.databinding.FragmentHomeBinding
import com.mobile.soundscape.evaluation.EvaluationPopupDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    override fun onResume() {
        super.onResume()
        // 홈 화면으로 돌아올 때마다 DB를 다시 확인해서 UI를 갱신
        loadHistoryAndSetupUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        com.bumptech.glide.Glide.with(this)
            .load(R.drawable.orb_animation)
            .into(binding.centerButton)
    }

    private fun loadHistoryAndSetupUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            // DB에서 최근 기록 6개 가져오기
            val historyList = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).historyDao().getRecentHistory()
            }

            if (historyList.isNotEmpty()) {
                // [A안] 기록이 있는 경우
                updateHomeUIWithHistory(historyList)
            } else {
                // [B안] 기록이 없는 경우 (초기 상태)
                setupDefaultUI()
            }
        }
    }

    // [B안] 데이터가 없을 때 호출되는 함수
    private fun setupDefaultUI() {
        binding.apply {
            startButton.visibility = View.VISIBLE
            tvSubtitle2.text = "시작하기 버튼으로 플레이리스트\n추천을 받을 수 있어요"

            // 하단 버튼 클릭 시 추천 시작
            startButton.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_recPlaceFragment)
            }

            // 데이터가 없을 땐 중앙 구슬 클릭 리스너 제거 (혹은 유지하고 싶다면 동일하게 설정 가능)
            centerButton.setOnClickListener(null)
        }
    }

    // [A안] 데이터가 있을 때 호출되는 함수
    private fun updateHomeUIWithHistory(historyList: List<PlaylistHistory>) {
        binding.apply {
            startButton.visibility = View.GONE
            tvSubtitle2.text = "최근 추천받은 몰입 테마예요\n중앙 버튼을 눌러 새로 시작할 수 있어요"

            centerButton.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_recPlaceFragment)
            }

            val wrappers = listOf(historyWrapper1, historyWrapper2, historyWrapper3, historyWrapper4, historyWrapper5, historyWrapper6)
            val icons = listOf(historyIcon1, historyIcon2, historyIcon3, historyIcon4, historyIcon5, historyIcon6)
            val texts = listOf(historyTxt1, historyTxt2, historyTxt3, historyTxt4, historyTxt5, historyTxt6)

            historyList.forEachIndexed { index, data ->
                if (index < wrappers.size) {
                    val wrapper = wrappers[index]
                    wrapper.visibility = View.VISIBLE

                    val targetPlaylistId = data.playlistId.toString()
                    val targetPlace = data.place
                    val targetGoal = data.goal

                    texts[index].text = "$targetPlace • $targetGoal"

                    val resId = resources.getIdentifier(data.iconResName, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        icons[index].setImageResource(resId)
                    }

                    // 이제 리스너는 루프 당시 고정되었던 targetPlaylistId를 기억합니다.
                    android.util.Log.d("HomeHistory", "Index: $index, ID: ${data.playlistId}")
                    wrapper.setOnClickListener {
                        val args = Bundle().apply {
                            putString("playlistId", targetPlaylistId)
                            putBoolean("isHistory", true)
                            putString("place", targetPlace)
                            putString("goal", targetGoal)
                        }

                        findNavController().navigate(
                            R.id.action_homeFragment_to_listFragment,
                            args
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}