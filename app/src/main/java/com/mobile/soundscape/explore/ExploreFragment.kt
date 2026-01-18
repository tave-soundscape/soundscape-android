package com.mobile.soundscape.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.ExploreResponse
import com.mobile.soundscape.api.dto.PlaceDetail
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import com.mobile.soundscape.databinding.FragmentExploreBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: ExploreCategoryAdapter
    private lateinit var playlistAdapter: ExploreAdapter
    private var playlistDataList = mutableListOf<PlaceDetail>()

    private var currentMainCategory: String = "장소"
    private var selectedSubCategory: String = "집/실내"
    private var playlistScrollState: Parcelable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCategoryTabs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPlaylists.layoutManager = LinearLayoutManager(context)

        binding.layoutDropdown.setOnClickListener {
            val bottomSheet = ExploreBottomSheetFragment { selectedName ->
                currentMainCategory = selectedName
                binding.tvCurrentCategory.text = selectedName
                updateCategoryTabs(selectedName)
            }
            bottomSheet.show(parentFragmentManager, "ExploreBottomSheet")
        }

        categoryAdapter = ExploreCategoryAdapter(getCategoryList(currentMainCategory)) { category ->
            fetchPlaylists(binding.tvCurrentCategory.text.toString(), category)
        }
        binding.rvCategoryTabs.adapter = categoryAdapter

        playlistAdapter = ExploreAdapter(
            items = playlistDataList,
            onAddClick = { item -> showAddBottomSheet(item) },
            onPlayClick = { item -> showSpotifyPlayBottomSheet(item) },
            onDetailClick = { item ->
                playlistScrollState = binding.rvPlaylists.layoutManager?.onSaveInstanceState()
                val bundle = Bundle().apply {
                    putString("playlistId", item.id.toString())
                    putString("title", item.title)
                    putString("subtitle", selectedSubCategory)
                }
                findNavController().navigate(R.id.action_exploreFragment_to_exploreDetailFragment, bundle)
            }
        )
        binding.rvPlaylists.adapter = playlistAdapter

        binding.tvCurrentCategory.text = currentMainCategory
        updateCategoryTabs(currentMainCategory)
    }

    private fun updateCategoryTabs(type: String) {
        val newTabs = getCategoryList(type)
        if (newTabs.isNotEmpty()) {
            val tabToSelect = if (newTabs.contains(selectedSubCategory)) selectedSubCategory else newTabs[0]
            selectedSubCategory = tabToSelect
            categoryAdapter.updateData(newTabs, tabToSelect)
            fetchPlaylists(type, tabToSelect)
        }
    }

    private fun getCategoryList(type: String): List<String> = when(type) {
        "장소" -> listOf("집/실내", "공원", "카페", "도서관", "이동 중", "헬스장", "코워킹")
        "목표" -> listOf("집중", "휴식", "수면", "활력", "위로", "분노", "미선택")
        "소음" -> listOf("조용함", "적당함", "시끄러움")
        else -> emptyList()
    }

    private fun fetchPlaylists(type: String, value: String) {
        selectedSubCategory = value
        val englishValue = translateToEnglish(value)
        val call = when(type) {
            "장소" -> RetrofitClient.exploreApi.getExploreByLocation(englishValue)
            "소음" -> RetrofitClient.exploreApi.getExploreByDecibel(englishValue)
            "목표" -> RetrofitClient.exploreApi.getExploreByGoal(englishValue)
            else -> return
        }

        call.enqueue(object : Callback<ExploreResponse> {
            override fun onResponse(call: Call<ExploreResponse>, response: Response<ExploreResponse>) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.data?.playlists ?: emptyList()
                    playlistAdapter.updateData(playlists.toMutableList())
                    playlistScrollState?.let { binding.rvPlaylists.layoutManager?.onRestoreInstanceState(it) }
                    fetchDetailsForItems(playlists.toMutableList())
                }
            }
            override fun onFailure(call: Call<ExploreResponse>, t: Throwable) { Log.e("Explore", "Failure: ${t.message}") }
        })
    }

    private fun fetchDetailsForItems(items: MutableList<PlaceDetail>) {
        items.forEachIndexed { index, item ->
            RetrofitClient.exploreApi.getExploreDetail(item.id.toString()).enqueue(object : Callback<BaseResponse<PlaceDetail>> {
                override fun onResponse(call: Call<BaseResponse<PlaceDetail>>, response: Response<BaseResponse<PlaceDetail>>) {
                    if (response.isSuccessful) response.body()?.data?.let { updateSinglePlaylist(index, it) }
                }
                override fun onFailure(call: Call<BaseResponse<PlaceDetail>>, t: Throwable) {}
            })
        }
    }

    private fun updateSinglePlaylist(index: Int, detailData: PlaceDetail) {
        val adapterList = playlistAdapter.getItemList()
        if (index < adapterList.size) {
            adapterList[index] = detailData
            playlistAdapter.notifyItemChanged(index)
            if (index == adapterList.size - 1) {
                binding.rvPlaylists.post {
                    playlistScrollState?.let {
                        binding.rvPlaylists.layoutManager?.onRestoreInstanceState(it)
                        playlistScrollState = null
                    }
                }
            }
        }
    }

    private fun showAddBottomSheet(item: PlaceDetail) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_explore_add, null)
        bottomSheetDialog.setContentView(view)
        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        etName.setText(item.title)
        view.findViewById<View>(R.id.btnAddLibrary).setOnClickListener {
            savePlaylistToLibrary(item.id.toString(), etName.text.toString())
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun showSpotifyPlayBottomSheet(item: PlaceDetail) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_spotify_play, null)
        bottomSheetDialog.setContentView(view)
        view.findViewById<View>(R.id.btnSpotifyPlay).setOnClickListener {
            sendAnalyticsData(item.id.toString())
            item.playlistUrl?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun savePlaylistToLibrary(id: String, name: String) {
        RetrofitClient.recommendationApi.updatePlaylistName(id, UpdatePlaylistNameRequest(name)).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) { showCustomToast("라이브러리에 추가되었습니다.") }
            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {}
        })
    }

    private fun sendAnalyticsData(id: String) { RetrofitClient.recommendationApi.sendAnalytics(id).enqueue(object : Callback<BaseResponse<String>> { override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {} override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {} }) }

    private fun showCustomToast(message: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message
        Toast(requireContext()).apply { duration = Toast.LENGTH_SHORT; view = layout; setGravity(Gravity.BOTTOM, 0, 300); show() }
    }

    private fun translateToEnglish(korean: String): String = when(korean) {
        "집/실내" -> "home"; "공원" -> "park"; "카페" -> "cafe"; "도서관" -> "library"; "이동 중" -> "moving"
        "코워킹" -> "co-working"; "헬스장" -> "gym"; "집중" -> "focus"; "휴식" -> "relax"; "수면" -> "sleep"
        "활력" -> "active"; "분노" -> "anger"; "위로" -> "consolation"; "미선택" -> "neutral"
        "조용함" -> "quiet"; "적당함" -> "moderate"; "시끄러움" -> "loud"; else -> korean.lowercase()
    }

    override fun onDestroyView() { super.onDestroyView() ; _binding = null }
}