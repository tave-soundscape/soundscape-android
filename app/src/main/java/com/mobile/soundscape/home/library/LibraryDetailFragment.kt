package com.mobile.soundscape.home.library

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
import com.mobile.soundscape.api.dto.LibraryPlaylistDetailResponse
import com.mobile.soundscape.databinding.FragmentLibraryDetailBinding // 수정된 레이아웃 바인딩
import com.mobile.soundscape.result.MusicModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// 1. 여기서 우리가 새로 만든 레이아웃(fragment_library_detail)을 연결합니다.
class LibraryDetailFragment : Fragment(R.layout.fragment_library_detail) {

    private var _binding: FragmentLibraryDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryDetailBinding.bind(view)

        // 2. Bundle에서 전달받은 데이터 꺼내기 (title, date, songs)
        val playlistId = arguments?.getString("playlistId")
        val title = arguments?.getString("title") ?: "알 수 없는 플레이리스트"
        // val date = arguments?.getString("date") ?: ""
        val songs = arguments?.getSerializable("songs") as? ArrayList<MusicModel> ?: arrayListOf()

        // 3. UI 데이터 연결
        binding.tvDetailPlaylistName.text = title
        // binding.tvDetailDescription.text = "${date}에 생성된
        // 플레이리스트"
        binding.tvSubtitle.text = "곡 ${songs.size}개"

        // 4. 투명 배경 어댑터 연결 (LibraryDetailAdapter 사용)
        val detailAdapter = LibraryDetailAdapter(songs)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = detailAdapter
        }

        // 5. 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            // 이전 화면(LibraryFragment)으로 돌아가기
            findNavController().popBackStack()
        }

        // id를 파라미터로 하는 API 호출하여 상세 정보 가져오기
        if (playlistId != null) {
            fetchPlaylistDetail(playlistId)
        } else {
            Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.\n다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    // id를 보내서 song detail 받기
    private fun fetchPlaylistDetail(id: String) {
        RetrofitClient.libraryApi.getPlaylistDetail(id).enqueue(object : Callback<BaseResponse<LibraryPlaylistDetailResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<LibraryPlaylistDetailResponse>>,
                response: Response<BaseResponse<LibraryPlaylistDetailResponse>>
            ) {
                if (response.isSuccessful) {
                    val songDetailData = response.body()?.data
                    if (songDetailData != null) {
                        updateUI(songDetailData)
                    } else {
                        Log.e("LibraryDetail", "Data is null")
                    }
                } else {
                    Log.e("LibraryDetail", "Error Code: ${response.code()}")
                    Toast.makeText(context, "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<LibraryPlaylistDetailResponse>>, t: Throwable) {
                Log.e("LibraryDetail", "Network Error: ${t.message}")
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(data: LibraryPlaylistDetailResponse) {
        binding.tvDetailPlaylistName.text = data.playlistName
        // 곡 개수 표시
        // TODO: 곡 갯수 말고 장소/목표로 바꾸기
        binding.tvSubtitle.text = "곡 ${data.songs.size}개"

        binding.btnDeepLinkSpotify.setOnClickListener {
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


        // API의 Song 객체를 UI용 MusicModel로 변환
        // (서버의 Song 필드명과 앱의 MusicModel 필드명이 다를 수 있으므로 매핑이 필요합니다)
        val musicList = data.songs.map { song ->
            MusicModel(
                // 예시: id = song.id,
                title = song.title,       // DTO의 필드명 확인 필요
                artist = song.artistName,     // DTO의 필드명 확인 필요
                // 이미지 URL이 있다면 넣고, 없다면 기본 리소스 사용
                albumCover = song.imageUrl,
                trackUri = song.uri // 기본 이미지 (필요시)
            )
        }

        // 리사이클러뷰 어댑터 연결
        val detailAdapter = LibraryDetailAdapter(musicList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = detailAdapter
        }

        val headerImageViews = listOf(
            binding.ivCover1,
            binding.ivCover2,
            binding.ivCover3,
            binding.ivCover4
        )

        // 리스트에서 앞에서부터 순서대로 이미지를 꺼내 헤더에 넣음
        for (i in headerImageViews.indices) {
            if (i < musicList.size) {
                // 각 ImageView에 URL 로드
                loadUrlToImageView(headerImageViews[i], musicList[i].albumCover)
            }
        }

        // 배경 그라데이션 이미지 (첫 번째 곡의 커버를 배경으로 사용)
        if (musicList.isNotEmpty()) {
            loadUrlToImageView(binding.ivBackgroundGradient, musicList[0].albumCover)
        }
    }

    private fun loadUrlToImageView(imageView: ImageView, url: String) {
        if (url.isNotEmpty()) {
            // Fragment에서는 this를 그대로 사용해도 Glide가 지원함 (viewLifecycleOwner 권장하지만 this도 가능)
            Glide.with(this)
                .load(url)
                .transform(CenterCrop()) // 꽉 채우기
                .into(imageView)
        } else {
            // URL이 비어있을 때 보여줄 기본 색상
            imageView.setImageResource(R.color.black)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, date: String, songs: List<MusicModel>): LibraryDetailFragment {
            val fragment = LibraryDetailFragment()
            val args = Bundle()
            args.putString("title", title)
            // args.putString("date", date) // 날짜 추가
            args.putSerializable("songs", ArrayList(songs))
            fragment.arguments = args
            return fragment
        }
    }
}