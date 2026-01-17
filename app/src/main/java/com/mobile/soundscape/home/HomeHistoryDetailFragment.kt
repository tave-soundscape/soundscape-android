package com.mobile.soundscape.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.RecommendationRequest
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import com.mobile.soundscape.data.RecommendationManager
import com.mobile.soundscape.databinding.FragmentHomeHistoryDetailBinding
import com.mobile.soundscape.databinding.FragmentListBinding
import com.mobile.soundscape.recommendation.RecommendationViewModel
import com.mobile.soundscape.result.MusicModel
import com.mobile.soundscape.result.PlaylistResultAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.isNotEmpty

class HomeHistoryDetailFragment : Fragment() {

    private var _binding: FragmentHomeHistoryDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationViewModel by activityViewModels()
    private lateinit var adapter: HomeHistoryDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 번들 데이터 읽기
        val playlistId = arguments?.getString("playlistId")
        val isHistory = arguments?.getBoolean("isHistory", false) ?: false
        val hPlace = arguments?.getString("place") ?: ""
        val hGoal = arguments?.getString("goal") ?: ""

        // 모드에 따른 분기
        if (isHistory && playlistId != null) {
            // [히스토리 모드] 서버에서 직접 가져오기
            loadPlaylistDetail(playlistId, hPlace, hGoal)
        } else {
            // [일반 추천 모드] 창고에서 꺼내기
            val data = RecommendationManager.cachedPlaylist
            val place = RecommendationManager.place ?: ""
            val goal = RecommendationManager.goal ?: ""

            if (data != null) {
                setupButtons(data)
                updateUIWithRealData(data, place, goal)
            } else {
                Toast.makeText(context, "데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadPlaylistDetail(id: String, place: String, goal: String) {
        RetrofitClient.recommendationApi.getPlaylistDetail(id).enqueue(object : Callback<BaseResponse<RecommendationResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<RecommendationResponse>>,
                response: Response<BaseResponse<RecommendationResponse>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        setupButtons(data)
                        updateUIWithRealData(data, place, goal)
                    }
                } else {
                    context?.let { ctx ->
                        Toast.makeText(ctx, "플레이리스트 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse<RecommendationResponse>>, t: Throwable) {
                // 로그 제거됨
            }
        })
    }

    // --- 받아온 데이터로 화면 채우기 ---
    private fun updateUIWithRealData(data: RecommendationResponse, place: String, goal: String) {

        val koreanGoal = when (goal.lowercase()) {
            "sleep" -> "수면"
            "focus" -> "집중"
            "consolation" -> "위로"
            "active" -> "활력"
            "stabilization" -> "안정"
            "anger" -> "분노"
            "relax" -> "휴식"
            "neutral" -> "미선택"
            else -> goal
        }

        binding.tvSubtitle.text = "$place · $koreanGoal"

        val finalName = if (!data.playlistName.isNullOrEmpty()) {
            data.playlistName
        } else {
            "맞춤 플레이리스트"
        }
        binding.tvPlaylistName.text = finalName

        val songs = data.songs ?: emptyList()

        val uiList = data.songs.map { song ->
            MusicModel(
                title = song.title,
                artist = song.artistName,
                albumCover = song.imageUrl,
                trackUri = song.uri
            )
        }

        setupRecyclerView(uiList)
        setupHeaderImages(uiList)
    }

    private fun setupButtons(data: RecommendationResponse) {
        // spotify deep link
        binding.btnDeepLinkSpotify.setOnClickListener {
            // Analytics 전송
            RetrofitClient.recommendationApi.sendAnalytics(data.playlistId.toString()).enqueue(object : Callback<BaseResponse<String>> {
                override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {}
                override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {}
            })

            val spotifyUrl = data.playlistUrl
            if (!spotifyUrl.isNullOrEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "스포티파이 링크 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView(songList: List<MusicModel>) {
        adapter = HomeHistoryDetailAdapter(songList)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@HomeHistoryDetailFragment.adapter
        }
    }

    private fun setupHeaderImages(songList: List<MusicModel>) {
        val headerImageViews = listOf(
            binding.ivCover1,
            binding.ivCover2,
            binding.ivCover3,
            binding.ivCover4
        )

        for (i in headerImageViews.indices) {
            if (i < songList.size) {
                loadUrlToImageView(headerImageViews[i], songList[i].albumCover)
            }
        }

        if (songList.isNotEmpty()) {
            loadUrlToImageView(binding.ivBackgroundGradient, songList[0].albumCover)
        }
    }

    private fun loadUrlToImageView(imageView: ImageView, url: String) {
        if (url.isNotEmpty()) {
            Glide.with(this)
                .load(url)
                .transform(CenterCrop())
                .into(imageView)
        } else {
            imageView.setImageResource(R.color.black)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}