package com.mobile.soundscape.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCategoryTabs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPlaylists.layoutManager = LinearLayoutManager(context)

        binding.layoutDropdown.setOnClickListener {
            val bottomSheet = ExploreBottomSheetFragment { selectedName ->
                binding.tvCurrentCategory.text = selectedName
                updateCategoryTabs(selectedName)
            }
            bottomSheet.show(parentFragmentManager, "ExploreBottomSheet")
        }

        categoryAdapter = ExploreCategoryAdapter(getCategoryList("장소")) { category ->
            fetchPlaylists(binding.tvCurrentCategory.text.toString(), category)
        }
        binding.rvCategoryTabs.adapter = categoryAdapter

        // [핵심 수정] 어댑터 생성 시 추가 버튼과 재생 버튼 콜백을 모두 연결합니다.
        playlistAdapter = ExploreAdapter(
            items = playlistDataList,
            onAddClick = { item -> showAddBottomSheet(item) },
            onPlayClick = { item -> showSpotifyPlayBottomSheet(item) }
        )
        binding.rvPlaylists.adapter = playlistAdapter

        updateCategoryTabs("장소")
    }

    // --- 데이터 로드 로직 (기존과 동일) ---
    private fun updateCategoryTabs(type: String) {
        val newTabs = getCategoryList(type)
        categoryAdapter.updateData(newTabs)
        if (newTabs.isNotEmpty()) {
            fetchPlaylists(type, newTabs[0])
        }
    }

    private fun getCategoryList(type: String): List<String> {
        return when(type) {
            "장소" -> listOf("집/실내", "공원", "카페", "도서관", "이동 중", "헬스장", "코워킹")
            "목표" -> listOf("집중", "휴식", "수면", "활력", "위로", "분노", "미선택")
            "소음" -> listOf("조용함", "적당함", "시끄러움")
            else -> emptyList()
        }
    }

    private fun fetchPlaylists(type: String, value: String) {
        val api = RetrofitClient.exploreApi
        val englishValue = translateToEnglish(value)
        val call = when(type) {
            "장소" -> api.getExploreByLocation(englishValue)
            "소음" -> api.getExploreByDecibel(englishValue)
            "목표" -> api.getExploreByGoal(englishValue)
            else -> return
        }

        call.enqueue(object : Callback<ExploreResponse> {
            override fun onResponse(call: Call<ExploreResponse>, response: Response<ExploreResponse>) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.data?.playlists ?: emptyList()
                    val mutablePlaylists = playlists.toMutableList()
                    playlistAdapter.updateData(mutablePlaylists)
                    fetchDetailsForItems(mutablePlaylists)
                }
            }
            override fun onFailure(call: Call<ExploreResponse>, t: Throwable) {
                Log.e("Explore", "Failure: ${t.message}")
            }
        })
    }

    private fun fetchDetailsForItems(items: MutableList<PlaceDetail>) {
        items.forEachIndexed { index, item ->
            RetrofitClient.exploreApi.getExploreDetail(item.id.toString()).enqueue(object : Callback<BaseResponse<PlaceDetail>> {
                override fun onResponse(call: Call<BaseResponse<PlaceDetail>>, response: Response<BaseResponse<PlaceDetail>>) {
                    if (response.isSuccessful) {
                        val detailData = response.body()?.data
                        if (detailData != null) {
                            updateSinglePlaylist(index, detailData, items)
                        }
                    }
                }
                override fun onFailure(call: Call<BaseResponse<PlaceDetail>>, t: Throwable) {
                    Log.e("Explore", "상세 정보 로드 실패: ${t.message}")
                }
            })
        }
    }

    private fun updateSinglePlaylist(index: Int, detailData: PlaceDetail, items: MutableList<PlaceDetail>) {
        val adapterList = playlistAdapter.getItemList()
        if (index < adapterList.size) {
            adapterList[index] = detailData
            playlistAdapter.notifyItemChanged(index)
        }
    }

    // --- 바텀시트 및 추가 기능 로직 ---

    // 1. 라이브러리 추가 바텀시트
    private fun showAddBottomSheet(item: PlaceDetail) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_explore_add, null)
        bottomSheetDialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnAdd = view.findViewById<View>(R.id.btnAddLibrary)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        etName.setText(item.title)
        etName.setSelection(item.title.length)

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        btnAdd.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isNotEmpty()) {
                savePlaylistToLibrary(item.id.toString(), newName)
                showCustomToast("내 라이브러리에 추가됐어요")
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheetDialog.show()
    }

    // 2. [신규 추가] 스포티파이 재생 바텀시트
    private fun showSpotifyPlayBottomSheet(item: PlaceDetail) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_spotify_play, null)
        bottomSheetDialog.setContentView(view)

        val btnPlay = view.findViewById<View>(R.id.btnSpotifyPlay)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        btnPlay.setOnClickListener {
            // 통계 데이터 전송
            sendAnalyticsData(item.id.toString())

            // 스포티파이 연결
            val spotifyUrl = item.playlistUrl
            if (!spotifyUrl.isNullOrEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
                    startActivity(intent)
                    bottomSheetDialog.dismiss()
                } catch (e: Exception) {
                    showCustomToast("링크를 열 수 없습니다.")
                }
            } else {
                showCustomToast("스포티파이 링크 정보가 없습니다.")
            }
        }
        bottomSheetDialog.show()
    }

    private fun savePlaylistToLibrary(playlistId: String, newName: String) {
        val requestBody = UpdatePlaylistNameRequest(newPlaylistName = newName)
        RetrofitClient.recommendationApi.updatePlaylistName(playlistId, requestBody)
            .enqueue(object : Callback<BaseResponse<String>> {
                override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {
                    if (response.isSuccessful) Log.d("EXPLORE_SAVE", "저장 성공")
                }
                override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                    Log.e("EXPLORE_SAVE", "통신 에러: ${t.message}")
                }
            })
    }

    private fun sendAnalyticsData(playlistId: String) {
        RetrofitClient.recommendationApi.sendAnalytics(playlistId).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {}
            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {}
        })
    }

    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast_custom, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 300)
        toast.show()
    }

    private fun translateToEnglish(korean: String): String {
        return when(korean) {
            "집/실내" -> "home"
            "공원" -> "park"
            "카페" -> "cafe"
            "도서관" -> "library"
            "이동 중" -> "moving"
            "코워킹" -> "co-working"
            "헬스장" -> "gym"
            "집중" -> "focus"
            "휴식" -> "relax"
            "수면" -> "sleep"
            "활기" -> "active"
            "분노" -> "anger"
            "위로" -> "consolation"
            "미선택" -> "neutral"
            "조용함" -> "quiet"
            "적당함" -> "moderate"
            "시끄러움" -> "loud"
            else -> korean.lowercase()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}