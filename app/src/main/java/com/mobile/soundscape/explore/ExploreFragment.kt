package com.mobile.soundscape.explore

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
import com.bumptech.glide.R
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    // 1. binding 변수 선언 (에러 해결 핵심)
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: ExploreCategoryAdapter
    private lateinit var playlistAdapter: ExploreAdapter

    private var playlistDataList = mutableListOf<PlaceDetail>()
    // 2. onCreateView 추가 (레이아웃 인플레이트)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 레이아웃 매니저 설정 (XML에 안 되어 있을 경우를 대비)
        binding.rvCategoryTabs.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPlaylists.layoutManager = LinearLayoutManager(context)

        binding.layoutDropdown.setOnClickListener {
            val bottomSheet = ExploreBottomSheetFragment { selectedName ->
                binding.tvCurrentCategory.text = selectedName
                updateCategoryTabs(selectedName) // 여기서 getCategoryList(selectedName)을 호출하게 됨
            }
            bottomSheet.show(parentFragmentManager, "ExploreBottomSheet")
        }

        // 2. 카테고리 탭 어댑터 설정 (초기값: 장소)
        categoryAdapter = ExploreCategoryAdapter(getCategoryList("장소")) { category ->
            fetchPlaylists(binding.tvCurrentCategory.text.toString(), category)
        }
        binding.rvCategoryTabs.adapter = categoryAdapter

        // 3. 플레이리스트 어댑터 설정
        playlistAdapter = ExploreAdapter(playlistDataList)
        binding.rvPlaylists.adapter = playlistAdapter

        // 초기 데이터 로드 (첫 화면 진입 시)
        updateCategoryTabs("장소")
    }

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

                    // [변경] 가변 리스트로 변환하여 어댑터에 먼저 껍데기 세팅
                    val mutablePlaylists = playlists.toMutableList()
                    playlistAdapter.updateData(mutablePlaylists)

                    // [핵심] 라이브러리 방식: 각 아이템별로 상세 정보(이미지) 긁어오기
                    fetchDetailsForItems(mutablePlaylists)
                }
            }
            override fun onFailure(call: Call<ExploreResponse>, t: Throwable) {
                Log.e("Explore", "Failure: ${t.message}")
            }
        })
    }

    // 라이브러리의 fetchDetailsForNewItems와 같은 역할
    private fun fetchDetailsForItems(items: MutableList<PlaceDetail>) {
        items.forEachIndexed { index, item ->
            RetrofitClient.exploreApi.getExploreDetail(item.id.toString()).enqueue(object : Callback<BaseResponse<PlaceDetail>> {
                override fun onResponse(call: Call<BaseResponse<PlaceDetail>>, response: Response<BaseResponse<PlaceDetail>>) {
                    if (response.isSuccessful) {
                        val detailData = response.body()?.data
                        if (detailData != null) {
                            // 데이터 업데이트 및 해당 칸만 새로고침
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
        // 어댑터가 관리하는 리스트의 해당 위치 데이터를 교체
        val adapterList = playlistAdapter.getItemList()
        if (index < adapterList.size) {
            adapterList[index] = detailData
            // 특정 아이템만 다시 그리도록 신호 보냄
            playlistAdapter.notifyItemChanged(index)
        }
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
    private fun showAddBottomSheet(item: PlaceDetail) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // 에러 해결 1: R.layout.bottom_sheet_explore_add 가 빨간색이면 파일 이름이 정확한지 확인하세요!
        val view = layoutInflater.inflate(com.mobile.soundscape.R.layout.bottom_sheet_explore_add, null)
        bottomSheetDialog.setContentView(view)

        // 에러 해결 2: findViewById는 인플레이트한 'view'에서 찾아야 합니다.
        val etName = view.findViewById<EditText>(com.mobile.soundscape.R.id.etPlaylistName)
        val btnAdd = view.findViewById<View>(com.mobile.soundscape.R.id.btnAddLibrary)
        val btnClose = view.findViewById<View>(com.mobile.soundscape.R.id.btnClose)

        // 초기 데이터 세팅
        etName.setText(item.title)
        etName.setSelection(item.title.length)

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        btnAdd.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isNotEmpty()) {
                savePlaylistToLibrary(item.id.toString(), newName)

                // 에러 해결 3: 아래에 주석을 풀고 만든 showCustomToast를 호출합니다.
                showCustomToast("내 라이브러리에 추가됐어요")
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        bottomSheetDialog.show()
    }

    // 2. 서버 통신
    private fun savePlaylistToLibrary(playlistId: String, newName: String) {
        val requestBody = UpdatePlaylistNameRequest(newPlaylistName = newName)

        RetrofitClient.recommendationApi.updatePlaylistName(playlistId, requestBody)
            .enqueue(object : Callback<BaseResponse<String>> {
                override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {
                    if (response.isSuccessful) {
                        Log.d("EXPLORE_SAVE", "저장 성공")
                    }
                }
                override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                    Log.e("EXPLORE_SAVE", "통신 에러: ${t.message}")
                }
            })
    }

    // 에러 해결 4: 주석을 풀어서 함수를 활성화합니다.
    private fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(com.mobile.soundscape.R.layout.toast_custom, null)
        layout.findViewById<TextView>(com.mobile.soundscape.R.id.tv_toast_message).text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 300)
        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}