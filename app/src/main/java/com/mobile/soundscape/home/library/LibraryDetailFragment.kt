package com.mobile.soundscape.home.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.client.RetrofitClient.libraryApi
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.LibraryPlaylistDetailResponse
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import com.mobile.soundscape.databinding.FragmentLibraryDetailBinding
import com.mobile.soundscape.result.MusicModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibraryDetailFragment : Fragment(R.layout.fragment_library_detail) {

    private var _binding: FragmentLibraryDetailBinding? = null
    private val binding get() = _binding!!

    //서버에서 받아온 상세 데이터를 저장해두는 변수
    private var currentPlaylistData: LibraryPlaylistDetailResponse? = null
    private var currentPlaylistId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryDetailBinding.bind(view)

        // 초기 데이터 세팅 (Bundle)
        currentPlaylistId = arguments?.getString("playlistId")
        val title = arguments?.getString("title") ?: "플레이리스트"
        val songs = arguments?.getSerializable("songs") as? ArrayList<MusicModel> ?: arrayListOf()

        // 우선 Bundle 데이터로 화면 그리기 (빠른 로딩)
        binding.tvDetailPlaylistName.text = title
        binding.tvSubtitle.text = "곡 ${songs.size}개"
        setupRecyclerView(songs) // 리사이클러뷰 연결 함수 분리

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // API 호출 및 더보기 버튼 설정
        if (currentPlaylistId != null) {
            // 상세 정보 가져오기
            fetchPlaylistDetail(currentPlaylistId!!)

            // 더보기 버튼 (API 호출 안 함! 저장된 데이터 사용)
            binding.btnMore.setOnClickListener {
                if (currentPlaylistData != null) {
                    showMoreBottomSheet()
                } else {
                    Toast.makeText(requireContext(), "정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    // --- [기능 1] 리사이클러뷰 설정 ---
    private fun setupRecyclerView(songs: List<MusicModel>) {
        val detailAdapter = LibraryDetailAdapter(songs)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = detailAdapter
        }
    }

    // --- [기능 2] 서버에서 상세 정보 가져오기 ---
    private fun fetchPlaylistDetail(id: String) {
        RetrofitClient.libraryApi.getPlaylistDetail(id).enqueue(object : Callback<BaseResponse<LibraryPlaylistDetailResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<LibraryPlaylistDetailResponse>>,
                response: Response<BaseResponse<LibraryPlaylistDetailResponse>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        // ★ [핵심] 데이터를 멤버 변수에 저장 (나중에 더보기 누를 때 씀)
                        currentPlaylistData = data
                        updateUI(data) // 화면 갱신
                    }
                } else {
                    Log.e("LibraryDetail", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BaseResponse<LibraryPlaylistDetailResponse>>, t: Throwable) {
                Log.e("LibraryDetail", "Network Error: ${t.message}")
            }
        })
    }

    // --- [기능 3] 화면 UI 업데이트 ---
    private fun updateUI(data: LibraryPlaylistDetailResponse) {
        // 텍스트 정보 갱신
        binding.tvDetailPlaylistName.text = data.playlistName
        binding.tvSubtitle.text = "곡 ${data.songs.size}개"

        // 스포티파이 링크 버튼
        binding.btnDeepLinkSpotify.setOnClickListener {
            val spotifyUrl = data.playlistUrl
            if (!spotifyUrl.isNullOrEmpty()) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl)))
                } catch (e: Exception) {
                    showCustomToast("링크를 열 수 없습니다.")
                }
            } else {
                showCustomToast("스포티파이 링크가 없습니다.")
            }
        }

        // Song 데이터 변환 및 리사이클러뷰 갱신
        val musicList = data.songs.map { song ->
            MusicModel(
                title = song.title,
                artist = song.artistName,
                albumCover = song.imageUrl,
                trackUri = song.uri
            )
        }
        setupRecyclerView(musicList)

        // 상단 헤더 4분할 이미지 로드
        val headerImageViews = listOf(binding.ivCover1, binding.ivCover2, binding.ivCover3, binding.ivCover4)
        for (i in headerImageViews.indices) {
            if (i < musicList.size) {
                loadUrlToImageView(headerImageViews[i], musicList[i].albumCover)
            }
        }

        // 배경 그라데이션
        if (musicList.isNotEmpty()) {
            loadUrlToImageView(binding.ivBackgroundGradient, musicList[0].albumCover)
        }
    }

    // --- [기능 4] 더보기 바텀시트 ---
    private fun showMoreBottomSheet() {
        val data = currentPlaylistData ?: return // 데이터 없으면 종료

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_library_more, null)
        bottomSheetDialog.setContentView(view)

        // 바텀시트 UI 세팅 (저장된 데이터 활용)
        val tvName = view.findViewById<TextView>(R.id.tvTitle) // ID 확인 필요
        val tvEdit = view.findViewById<TextView>(R.id.tvEditName) // 수정 버튼
        val tvDelete = view.findViewById<TextView>(R.id.tvDelete) // 삭제 버튼
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)


        // 앨범 커버 이미지뷰들
        val ivCover1 = view.findViewById<ImageView>(R.id.ivCover1)
        val ivCover2 = view.findViewById<ImageView>(R.id.ivCover2)
        val ivCover3 = view.findViewById<ImageView>(R.id.ivCover3)
        val ivCover4 = view.findViewById<ImageView>(R.id.ivCover4)

        // UI 채우기
        tvName?.text = data.playlistName

        // 썸네일 채우기 로직 (음악 목록이 있다면)
        val songs = data.songs
        val imageViews = listOf(ivCover1, ivCover2, ivCover3, ivCover4)
        if (!songs.isNullOrEmpty()) {
            for (i in imageViews.indices) {
                if (i < songs.size && imageViews[i] != null) {
                    loadUrlToImageView(imageViews[i]!!, songs[i].imageUrl)
                }
            }
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 수정 버튼 클릭
        tvEdit.setOnClickListener {
            bottomSheetDialog.dismiss() // 더보기 창 닫고
            showEditNameBottomSheet(data.playlistName) // 수정 창 열기
        }

        // 삭제 버튼 클릭 (추후 구현)
        tvDelete.setOnClickListener {
            bottomSheetDialog.dismiss() // 더보기 창 닫고
            showDeleteBottomSheet(data.playlistId.toString()) // 삭제 창 열기
        }

        bottomSheetDialog.show()
    }

    // --- [기능 5] 이름 수정 바텀시트 ---
    private fun showEditNameBottomSheet(currentName: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
        bottomSheetDialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmEdit)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        etName.setText(currentName)
        etName.setSelection(currentName.length) // 커서 끝으로

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        // 수정 완료 버튼
        btnConfirm.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updatePlaylistNameOnServer(newName, bottomSheetDialog)
            } else {
                showCustomToast("이름을 입력해주세요.")
            }
        }

        bottomSheetDialog.show()
    }

    // --- [기능 6] 이름 수정 API 호출 ---
    private fun updatePlaylistNameOnServer(newName: String, dialogToClose: BottomSheetDialog) {
        val playlistId = currentPlaylistId ?: return
        val requestBody = UpdatePlaylistNameRequest(newPlaylistName = newName)

        RetrofitClient.recommendationApi.updatePlaylistName(playlistId, requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {
                if (response.isSuccessful) {
                    // 성공 시 UI 즉시 반영
                    binding.tvDetailPlaylistName.text = newName

                    // 저장된 데이터(currentPlaylistData)의 이름도 갱신해줘야 함 (다음에 더보기 누를 때 반영되도록)
                    currentPlaylistData?.playlistName = newName

                    showCustomToast("이름이 수정되었습니다.")
                    dialogToClose.dismiss()
                } else {
                    showCustomToast("수정에 실패했습니다.")
                    Log.e("API_UPDATE", "Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                showCustomToast("네트워크 오류가 발생했습니다.")
                Log.e("API_UPDATE", "Error: ${t.message}")
            }
        })
    }

    // 이미지 로드 유틸 함수
    private fun loadUrlToImageView(imageView: ImageView, url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .transform(CenterCrop())
                .into(imageView)
        } else {
            imageView.setImageResource(R.color.black) // 기본 색상
        }
    }

    // 삭제 바텀시트
    private fun showDeleteBottomSheet(id: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_library_delete, null)
        bottomSheetDialog.setContentView(view)

        val btnConfirmDelete = view.findViewById<View>(R.id.btnConfirmDelete)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        btnConfirmDelete.setOnClickListener {
            deletePlaylistOnServer(id, bottomSheetDialog)
        }

        bottomSheetDialog.show()
    }

    private fun deletePlaylistOnServer(id: String, dialogToClose: BottomSheetDialog) {

        RetrofitClient.libraryApi.deletePlaylist(id).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    // 1. 성공 토스트 (삭제 아이콘 포함)
                    showCustomToast("플레이리스트가 삭제됐어요", isDelete = true)

                    // 2. 더보기 바텀시트 닫기
                    dialogToClose.dismiss()

                    // 3. ★중요★ 삭제된 플리 상세 화면에서 나가기 (목록으로 이동)
                    findNavController().popBackStack()

                } else {
                    showCustomToast("삭제를 실패했습니다.")
                    Log.e("API_DELETE", "Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                showCustomToast("네트워크 오류가 발생했습니다.")
                Log.e("API_DELETE", "Error: ${t.message}")
            }
        })
    }


    // 커스텀 토스트 - 기본값은 false
    private fun showCustomToast(message: String, isDelete: Boolean = false) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast_custom, null)
        layout.findViewById<TextView>(R.id.tv_toast_message).text = message

        val ivIcon = layout.findViewById<ImageView>(R.id.iv_toast_icon)
        if (isDelete) {
            ivIcon.setImageResource(R.drawable.icon_library_detail_delete)
            ivIcon.visibility = View.VISIBLE // 아이콘 보이게
        }

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

    // companion object는 필요하다면 유지
}