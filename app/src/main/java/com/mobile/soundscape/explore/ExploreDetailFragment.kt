package com.mobile.soundscape.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.PlaceDetail
import com.mobile.soundscape.databinding.FragmentExploreDetailBinding
import com.mobile.soundscape.home.library.LabelMapper
import com.mobile.soundscape.result.MusicModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreDetailFragment : Fragment(R.layout.fragment_explore_detail) {

    private var _binding: FragmentExploreDetailBinding? = null
    private val binding get() = _binding!!

    private var currentPlaylistId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExploreDetailBinding.bind(view)

        // 1. 초기 데이터 수신 (Bundle)
        currentPlaylistId = arguments?.getString("playlistId")
        val initialTitle = arguments?.getString("title") ?: "플레이리스트"
        val initialSubtitle = arguments?.getString("subtitle") ?: ""
        // 처음엔 곡이 0개일 확률이 높으므로 빈 리스트로 시작
        val initialSongs = arguments?.getSerializable("songs") as? ArrayList<MusicModel> ?: arrayListOf()

        // 2. 초기 UI 세팅 (서버 데이터 오기 전)
        binding.tvDetailPlaylistName.text = initialTitle
        binding.tvSubtitle.text = initialSubtitle
        setupRecyclerView(initialSongs)

        // 3. 서버에서 진짜 상세 데이터 가져오기 (곡 리스트 포함)
        if (currentPlaylistId != null) {
            fetchExploreDetail(currentPlaylistId!!)
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun fetchExploreDetail(id: String) {
        // [참고] ExploreApi에 정의된 상세조회 함수명으로 호출하세요 (예: getExploreDetail)
        RetrofitClient.exploreApi.getExploreDetail(id).enqueue(object : Callback<BaseResponse<PlaceDetail>> {
            override fun onResponse(call: Call<BaseResponse<PlaceDetail>>, response: Response<BaseResponse<PlaceDetail>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        Log.d("EXPLORE_LOG", "상세 데이터 수신 성공: ${data.songs?.size}곡")
                        updateUI(data)
                    }
                } else {
                    Log.e("EXPLORE_LOG", "에러 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BaseResponse<PlaceDetail>>, t: Throwable) {
                Log.e("EXPLORE_LOG", "네트워크 실패: ${t.message}")
            }
        })
    }

    private fun updateUI(data: PlaceDetail) {
        // 텍스트 정보 업데이트
        binding.tvDetailPlaylistName.text = data.title

        // 장소/목표 태그 조합
        val displaySubtitle = when {
            !data.averageDecibel.isNullOrBlank() -> LabelMapper.getKoreanDecibel(data.averageDecibel)
            !data.primaryGoal.isNullOrBlank() -> LabelMapper.getKoreanGoal(data.primaryGoal)
            else -> LabelMapper.getKoreanPlace(data.location ?: "")
        }
        binding.tvSubtitle.text = displaySubtitle

        // MusicModel로 데이터 변환 (Adapter 전용)
        val musicList = data.songs?.map { song ->
            MusicModel(
                title = song.title ?: "Unknown",
                artist = song.artistName ?: "Unknown",
                albumCover = song.imageUrl ?: "",
                trackUri = song.uri ?: ""
            )
        } ?: emptyList()

        // 리사이클러뷰 갱신
        setupRecyclerView(musicList)

        // 상단 4분할 이미지 갱신
        val headerImageViews = listOf(binding.ivCover1, binding.ivCover2, binding.ivCover3, binding.ivCover4)
        headerImageViews.forEachIndexed { i, imageView ->
            if (i < musicList.size) {
                loadUrlToImageView(imageView, musicList[i].albumCover)
            } else {
                imageView.setImageResource(R.color.black)
            }
        }

        // 배경 그라데이션 (첫 번째 곡 커버)
        if (musicList.isNotEmpty()) {
            loadUrlToImageView(binding.ivBackgroundGradient, musicList[0].albumCover)
        }

        // 스포티파이 버튼
        binding.btnDeepLinkSpotify.setOnClickListener {
            val url = data.playlistUrl
            if (!url.isNullOrEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } else {
                Toast.makeText(requireContext(), "링크가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView(songs: List<MusicModel>) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ExploreDetailAdapter(songs)
        }
    }

    private fun loadUrlToImageView(imageView: ImageView, url: String?) {
        if (!url.isNullOrEmpty() && url.startsWith("http")) {
            Glide.with(this).load(url).transform(CenterCrop()).into(imageView)
        } else {
            imageView.setImageResource(R.color.black)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}