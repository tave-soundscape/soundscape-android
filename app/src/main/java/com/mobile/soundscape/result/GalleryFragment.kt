package com.mobile.soundscape.playlist

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentGalleryBinding
import com.mobile.soundscape.result.GalleryAdapter
import com.mobile.soundscape.result.ListFragment
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.math.abs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.mobile.soundscape.MainActivity
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import com.mobile.soundscape.data.RecommendationManager
import com.mobile.soundscape.result.MusicModel

class GalleryFragment : Fragment() {

    // 1. 바인딩 변수 설정
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val TAG = "PlayTest"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 화면 세팅
        setupViewPagerSettings()

        // data 창고에서 가져오기
        val data = RecommendationManager.cachedPlaylist
        val place = RecommendationManager.place
        val goal = RecommendationManager.goal

        // 플레이리스트 제목 설정
        val savedPlaylistName = context?.let { ctx ->
            RecommendationManager.getPlaylistName(ctx)
        } ?: ""
        if (savedPlaylistName.isNotEmpty()) {
            binding.tvPlaylistName.text = savedPlaylistName
        } else {
            binding.tvPlaylistName.text = data?.playlistName ?: "플레이리스트 이름"
        }

        binding.tvPlaylistInfo.text = "$place · $goal"

        if (data != null) {
            // 화면 업데이트
            updateUIWithSharedData(data)
            // spotify deep link로 연결
            binding.btnDeepLinkSpotify.setOnClickListener {

                // 딥링크 누르면 백엔드로 클릭여부 보내기
                RetrofitClient.recommendationApi.sendAnalytics(data.playlistId.toString()).enqueue(object : Callback<BaseResponse<String>> {
                    override fun onResponse(
                        call: Call<BaseResponse<String>>,
                        response: Response<BaseResponse<String>>
                    ) {}
                    override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                    }
                })

                val spotifyUrl = data.playlistUrl

                if (!spotifyUrl.isNullOrEmpty()) {
                    try {
                        // 인텐트 생성 (ACTION_VIEW: 보여달라)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
                        // 실행 (스포티파이 앱이 있으면 앱으로, 없으면 브라우저로 켜짐)
                        startActivity(intent)
                    } catch (e: Exception) {
                        // 만약 링크를 열 수 있는 앱(브라우저 등)이 아예 없을 때 죽는 것 방지
                        Toast.makeText(requireContext(), "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "스포티파이 링크 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 리스트 모드로 변경 버튼
        binding.btnGalleryToList.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.playlist_fragment_container, ListFragment())
                    .commit()
            }
        }
        
        // 라이브러리에 추가 버튼
        binding.btnAddLibrary.setOnClickListener {
            val currentName = binding.tvPlaylistName.text.toString()
            showAddLibraryBottomSheet(currentName)
        }

        binding.btnMoveToLibrary.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            // 메인 액티비티를 다시 띄우면서 기존 스택 정리
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // 메인에서 라이브러리 탭을 열도록 신호 전달
            intent.putExtra("NAVIGATE_TO", "LIBRARY")
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // --- 뷰모델 데이터로 화면 그리기 ---
    private fun updateUIWithSharedData(data: RecommendationResponse) {

        // DTO -> MusicModel 변환
        val musicList = data.songs.map { song ->
            MusicModel(
                title = song.title,
                artist = song.artistName,
                albumCover = song.imageUrl,
                trackUri = song.uri
            )
        }

        if (musicList.isEmpty()) return

        // 어댑터 연결
        val adapter = GalleryAdapter(musicList)
        binding.vpGallery.adapter = adapter

        // 시작 위치 계산 (무한 스크롤 효과)
        val centerPosition = Int.MAX_VALUE / 2
        val startPosition = centerPosition - (centerPosition % musicList.size)
        binding.vpGallery.setCurrentItem(startPosition, false)

        // 초기 배경 및 텍스트 설정 (첫 곡)
        updateBackgroundAndText(musicList[0])

        // 페이지 변경 리스너 등록
        binding.vpGallery.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val realPosition = position % musicList.size
                updateBackgroundAndText(musicList[realPosition])
            }
        })
    }

    // 배경 블러 및 하단 텍스트 업데이트 헬퍼 함수
    private fun updateBackgroundAndText(music: MusicModel) {
        binding.tvCurrentTitle.text = music.title
        binding.tvCurrentArtist.text = music.artist

        context?.let { ctx ->
            Glide.with(ctx)
                .load(music.albumCover)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.ivBlurBackground)
        }
    }


    // ViewPager2 모양 설정 (Transformer 등)
    private fun setupViewPagerSettings() {
        val viewPager = binding.vpGallery
        viewPager.offscreenPageLimit = 3
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(20))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            val scaleFactor = 0.50f + r * 0.50f
            page.scaleY = scaleFactor
            page.scaleX = scaleFactor
            val myOffset = (page.width - (page.width * scaleFactor)) / 3

            if (position < 0) {
                page.translationX = myOffset
            } else {
                page.translationX = -myOffset
            }
        }
        viewPager.setPageTransformer(compositePageTransformer)
    }

    private fun showAddLibraryBottomSheet(currentName: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
        bottomSheetDialog.setContentView(view)

        val etName = view.findViewById<android.widget.EditText>(R.id.etPlaylistName)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmEdit)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        etName.setText(currentName)
        etName.setSelection(currentName.length)

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        btnConfirm.isEnabled = false
        btnConfirm.alpha = 0.4f

        etName.doAfterTextChanged { text ->
            val input = text.toString().trim()
            if (input.isNotEmpty()) {
                btnConfirm.isEnabled = true
                btnConfirm.alpha = 1.0f
            } else {
                btnConfirm.isEnabled = false
                btnConfirm.alpha = 0.4f
            }
        }

        btnConfirm.setOnClickListener {
            val newName = etName.text.toString().trim()
            binding.tvPlaylistName.text = newName
            // 수정된 플리 이름 내부 저장소에 저장
            context?.let { ctx ->
                RecommendationManager.savePlaylistName(ctx, newName)
            }
            // 저장한 id 백엔드로 보내기
            val savedPlaylistId = context?.let { ctx ->
                RecommendationManager.getPlaylistId(ctx)
            } ?: ""
            // 백엔드로 수정된 플리이름 보내는 함수
            updatePlaylistNameOnServer(savedPlaylistId, newName)

            showCustomToast("내 라이브러리에 추가됐어요")
            bottomSheetDialog.dismiss()

            // 라이브러리로 이동하는 버튼으로 교체
            binding.btnMoveToLibrary.visibility = View.VISIBLE
            binding.btnAddLibrary.visibility = View.GONE
        }
        bottomSheetDialog.show()
    }


    // --- 백엔드에 이름 수정 요청 보내기 ---
    private fun updatePlaylistNameOnServer(playlistId: String, newName: String) {
        val requestBody = UpdatePlaylistNameRequest(newPlaylistName = newName)

        // 2. API 호출
        RetrofitClient.recommendationApi.updatePlaylistName(playlistId, requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    // 성공 로그
                    Log.d(TAG, "이름 수정 성공: $newName")
                } else {
                    // 실패 로그 (하지만 이미 화면은 바꿨으니 조용히 로그만 남김)
                    Log.e(TAG, "수정 실패 Code: ${response.code()}")
                    Toast.makeText(context, "서버 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e("API_UPDATE", "통신 에러: ${t.message}")
            }
        })
    }

    fun showCustomToast(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast_custom, null)
        val textView = layout.findViewById<TextView>(R.id.tv_toast_message)
        textView.text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM, 0, 300)
        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}