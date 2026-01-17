package com.mobile.soundscape.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.mobile.soundscape.R
import com.mobile.soundscape.data.AppDatabase
import com.mobile.soundscape.data.PlaylistHistory
import com.mobile.soundscape.databinding.FragmentHomeHistoryBinding
import com.mobile.soundscape.result.ListFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeHistoryFragment : Fragment(R.layout.fragment_home_history) {

    private var _binding: FragmentHomeHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeHistoryBinding.bind(view)

        // 2. DB에서 데이터 로드 및 UI 업데이트
        loadHistory()

        // 3. 상단 탭 버튼 리스너 설정
        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        // '추천 받기' 버튼 클릭 -> HomeFragment(추천 화면)로 이동
        binding.btnRecommend.setOnClickListener {
            val popped = findNavController().popBackStack()
            if (!popped) {
                findNavController().navigate(R.id.action_homeHistoryFragment_to_homeFragment)
            }
        }

        // '최근 몰입' 버튼 클릭 -> 현재 화면이므로 새로고침
        binding.btnRecent.setOnClickListener {
            loadHistory()
        }
    }

    private fun loadHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            // IO 스레드에서 DB 조회
            val historyList = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).historyDao().getRecentHistory()
            }

            updateUI(historyList)
        }
    }

    private fun updateUI(historyList: List<PlaylistHistory>) {
        binding.apply {
            // XML에 정의된 7개의 아이템 뷰들을 리스트로 묶어서 관리
            // 0번(중앙) + 1~6번(주변) = 총 7개
            val wrappers = listOf(
                historyWrapper0, historyWrapper1, historyWrapper2, historyWrapper3,
                historyWrapper4, historyWrapper5, historyWrapper6
            )
            val icons = listOf(
                historyIcon0, historyIcon1, historyIcon2, historyIcon3,
                historyIcon4, historyIcon5, historyIcon6
            )
            val texts = listOf(
                historyTxt0, historyTxt1, historyTxt2, historyTxt3,
                historyTxt4, historyTxt5, historyTxt6
            )

            for (i in 0 until 7) {
                val wrapper = wrappers[i]
                val iconView = icons[i]
                val textView = texts[i]

                // 배경 Wrapper는 무조건 보이게 설정
                wrapper.visibility = View.VISIBLE

                // 해당 인덱스에 데이터가 있는지 확인
                if (i < historyList.size) {
                    val data = historyList[i]  // 데이터가 있는 경우 -> 채워넣기

                    textView.visibility = View.VISIBLE
                    textView.text = "${data.place} • ${data.goal}"

                    val resId = resources.getIdentifier(data.iconResName, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        iconView.setImageResource(resId)
                    } else {
                        iconView.setImageResource(R.drawable.place_icon1)
                    }

                    // 클릭 리스너 연결
                    wrapper.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("playlistId", data.playlistId.toString())
                            putBoolean("isHistory", true)
                            putString("place", data.place)
                            putString("goal", data.goal)
                        }
                        findNavController().navigate(R.id.action_homeHistoryFragment_to_homeHistoryDetailFragment, bundle)
                    }

                } else {
                    // 데이터가 없는 경우 빈 wrapper만 보여주기

                    iconView.visibility = View.VISIBLE
                    iconView.setImageDrawable(null)

                    textView.text = ""
                    textView.visibility = View.INVISIBLE

                    // 클릭 리스너 제거 (빈 곳 눌러도 반응 없게)
                    wrapper.setOnClickListener(null)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}