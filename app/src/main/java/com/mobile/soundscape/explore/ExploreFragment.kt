package com.mobile.soundscape.explore

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentExploreBinding


class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val placeCategories = listOf("집/실내", "공원", "카페", "코워킹", "헬스장", "도서관", "이동중")
    private val goalCategories = listOf("집중", "휴식", "수면", "활력", "독서", "분노", "위로")
    private val noiseCategories = listOf("백색소음", "빗소리", "파도소리", "시끄러움", "조용함")

    // 현재 보여줄 데이터를 담을 리스트 (가변 리스트)
    private var currentCategories = mutableListOf<String>().apply { addAll(placeCategories) }
    private lateinit var categoryAdapter: ExploreCategoryAdapter


    // 전체 원본 데이터 리스트 (나중에는 서버에서 가져옴)
    private val allPlaylists = listOf(
        ExplorePlaylist(1, "집중 빗소리", "공부용", "#빗소리", R.drawable.img_placeholder, "집/실내"),
        ExplorePlaylist(2, "공원 산책", "힐링용", "#자연", R.drawable.img_placeholder, "공원"),
        ExplorePlaylist(3, "카페 백색소음", "업무용", "#카페", R.drawable.img_placeholder, "카페")
    )

    // 현재 화면에 보여줄 필터링된 데이터 리스트
    private lateinit var playlistAdapter: ExplorePlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = ExploreCategoryAdapter(currentCategories) { selectedCategory ->
            Log.d("Explore", "선택된 카테고리: $selectedCategory")
            filterPlaylists(selectedCategory)
        }

        binding.rvCategoryTabs.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        playlistAdapter = ExplorePlaylistAdapter(allPlaylists)
        binding.rvPlaylists.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.layoutDropdown.setOnClickListener {
            val bottomSheet = ExploreBottomSheetFragment { selected ->

                binding.tvCurrentCategory.text = selected

                when (selected) {
                    "장소" -> updateCategoryTabs(placeCategories)
                    "목표" -> updateCategoryTabs(goalCategories)
                    "소음" -> updateCategoryTabs(noiseCategories)
                }
            }
            bottomSheet.show(parentFragmentManager, "ExploreBottomSheet")
        }
    }

    private fun updateCategoryTabs(newCategories: List<String>) {
        categoryAdapter.updateData(newCategories)

        // 탭이 바뀌면 항상 첫 번째 카테고리가 선택되도록 하려면 아래 줄 추가
        binding.rvCategoryTabs.scrollToPosition(0)

        filterPlaylists(newCategories[0])
    }

    private fun filterPlaylists(category: String) {

        val filteredList = allPlaylists.filter { it.category == category }

        playlistAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}