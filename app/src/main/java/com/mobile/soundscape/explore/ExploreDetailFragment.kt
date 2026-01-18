package com.mobile.soundscape.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
        val initialSongs = arguments?.getSerializable("songs") as? ArrayList<MusicModel> ?: arrayListOf()

        // 2. 초기 UI 세팅
        binding.tvDetailPlaylistName.text = initialTitle
        binding.tvSubtitle.text = initialSubtitle
        setupRecyclerView(initialSongs)

        // 3. 서버에서 상세 데이터 가져오기
        if (currentPlaylistId != null) {
            fetchExploreDetail(currentPlaylistId!!)
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun fetchExploreDetail(id: String) {
        RetrofitClient.exploreApi.getExploreDetail(id).enqueue(object : Callback<BaseResponse<PlaceDetail>> {
            override fun onResponse(call: Call<BaseResponse<PlaceDetail>>, response: Response<BaseResponse<PlaceDetail>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        updateUI(data)
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse<PlaceDetail>>, t: Throwable) {

            }
        })
    }

    private fun updateUI(data: PlaceDetail) {
        binding.tvDetailPlaylistName.text = data.title

        val displaySubtitle = when {
            !data.averageDecibel.isNullOrBlank() -> LabelMapper.getKoreanDecibel(data.averageDecibel)
            !data.primaryGoal.isNullOrBlank() -> LabelMapper.getKoreanGoal(data.primaryGoal)
            else -> LabelMapper.getKoreanPlace(data.location ?: "")
        }
        binding.tvSubtitle.text = displaySubtitle

        val musicList = data.songs?.map { song ->
            MusicModel(
                title = song.title ?: "Unknown",
                artist = song.artistName ?: "Unknown",
                albumCover = song.imageUrl ?: "",
                trackUri = song.uri ?: ""
            )
        } ?: emptyList()

        setupRecyclerView(musicList)

        val headerImageViews = listOf(binding.ivCover1, binding.ivCover2, binding.ivCover3, binding.ivCover4)
        headerImageViews.forEachIndexed { i, imageView ->
            if (i < musicList.size) {
                loadUrlToImageView(imageView, musicList[i].albumCover)
            } else {
                imageView.setImageResource(R.color.black)
            }
        }

        if (musicList.isNotEmpty()) {
            loadUrlToImageView(binding.ivBackgroundGradient, musicList[0].albumCover)
        }

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