package com.mobile.soundscape.result

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mobile.soundscape.R
import com.mobile.soundscape.databinding.FragmentListBinding
import com.mobile.soundscape.playlist.GalleryFragment
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.api.dto.UpdatePlaylistNameRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.MainActivity
import com.mobile.soundscape.PreferenceManager
import com.mobile.soundscape.api.dto.RecommendationRequest
import com.mobile.soundscape.data.RecommendationManager
import com.mobile.soundscape.data.RecommendationManager.getPlaylistName
import com.mobile.soundscape.recommendation.RecommendationViewModel
import kotlin.getValue


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    // 프래그먼트에서 바인딩은 get()을 통해 접근하는 것이 안전합니다.
    private val binding get() = _binding!!
    private val viewModel: RecommendationViewModel by activityViewModels()
    private val TAG = "PlayTest"
    private lateinit var adapter: PlaylistResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 바인딩 초기화
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 번들 데이터 읽기
        val playlistId = arguments?.getString("playlistId")
        val isHistory = arguments?.getBoolean("isHistory", false) ?: false
        val hPlace = arguments?.getString("place") ?: ""
        val hGoal = arguments?.getString("goal") ?: ""

        Log.d(TAG, "ListFragment: 진입 (isHistory: $isHistory, id: $playlistId)")

        // 2. 모드에 따른 분기
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
                Log.e(TAG, "데이터가 없습니다! (RecommendationManager 확인 필요)")
                Toast.makeText(context, "데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. 공통 버튼 리스너 (이동 및 뒤로가기)
        binding.btnMoveToLibrary.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_libraryFragment)
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
                        // 서버에서 받아온 상세 데이터로 UI 업데이트
                        setupButtons(data)
                        updateUIWithRealData(data, place, goal)
                    }
                } else {
                    Log.e(TAG, "상세 조회 실패 Code: ${response.code()}")
                    context?.let { ctx ->
                        Toast.makeText(ctx, "플레이리스트 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse<RecommendationResponse>>, t: Throwable) {
                Log.e(TAG, "통신 에러: ${t.message}")
            }
        })
    }
    // --- 받아온 데이터로 화면 채우기 ---
    private fun updateUIWithRealData(data: RecommendationResponse, place: String, goal: String) {

        Log.d(TAG, "ListFragment: UI 업데이트 함수 진입")

        val koreanGoal = when (goal.lowercase()) {
            "sleep" -> "수면"
            "focus" -> "집중"
            "cosolation" -> "위로"
            "active" -> "활력"
            "stabilization" -> "안정"
            "anger" -> "분노"
            "relax" -> "휴식"
            "neutral" -> "미선택"
            else -> goal // 혹시 모르니 기본값은 영어 그대로
        }

        binding.tvSubtitle.text = "$place · $koreanGoal"

        val finalName = if (!data.playlistName.isNullOrEmpty()) {
            data.playlistName // 서버가 보내준 이 구슬만의 고유 이름
        } else {
            "맞춤 플레이리스트" // 혹시 이름이 없을 때의 기본값
        }
        binding.tvPlaylistName.text = finalName

        val songs = data.songs ?: emptyList() // null이면 빈 리스트로 처리
        Log.d(TAG, "ListFragment: 리사이클러뷰에 넣을 곡 개수: ${songs.size}")

        // DTO(Song) -> MusicModel(UI용) 변환
        // imageUrl이 String으로 바뀌어서 처리가 훨씬 쉬워짐!
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

        // (참고) 플레이리스트 전체 링크 저장 (필요 시 사용)
        // val playlistUrl = data.playlistUrl
    }

    private fun setupButtons(data: RecommendationResponse) {
        // 플리 다시 만들기 (바텀시트)
//        binding.regenerateBtn.setOnClickListener {
//            showRegenerateBottomSheet()
//        }

        // 라이브러리 저장 (이름 수정 바텀시트)
        binding.btnAddLibrary.setOnClickListener {
            showAddLibraryBottomSheet(binding.tvPlaylistName.text.toString())
        }

        binding.tvPlaylistName.setOnClickListener {
            showAddLibraryBottomSheet(binding.tvPlaylistName.text.toString())
        }

        // 갤러리로 이동
        binding.btnListToGallery.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_galleryFragment)
        }

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

            // 스포티파이 딥링크로 연결
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

    private fun setupRecyclerView(songList: List<MusicModel>) {
        adapter = PlaylistResultAdapter(songList) {
            // 푸터 클릭 시 바텀시트
            showRegenerateBottomSheet()
        }
        binding.recyclerView.apply {
            // Fragment에서는 this@... 대신 requireContext() 사용
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ListFragment.adapter
        }
    }

    private fun setupHeaderImages(songList: List<MusicModel>) {
        // XML에 있는 4개의 이미지뷰 ID (ivCover1 ~ ivCover4)
        val headerImageViews = listOf(
            binding.ivCover1,
            binding.ivCover2,
            binding.ivCover3,
            binding.ivCover4
        )

        // 리스트에서 앞에서부터 순서대로 이미지를 꺼내 헤더에 넣음
        for (i in headerImageViews.indices) {
            if (i < songList.size) {
                // 각 ImageView에 URL 로드
                loadUrlToImageView(headerImageViews[i], songList[i].albumCover)
            }
        }

        // 배경 그라데이션 이미지 (첫 번째 곡의 커버를 배경으로 사용)
        if (songList.isNotEmpty()) {
            loadUrlToImageView(binding.ivBackgroundGradient, songList[0].albumCover)
        }
    }

    // --- 이미지 로드 헬퍼 함수 ---
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

    private fun showRegenerateBottomSheet() {
        // 다이얼로그 생성 (Context 필요 -> requireContext())
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        // 레이아웃(XML) 가져오기 (layoutInflater 접근 변경)
        val view = requireActivity().layoutInflater.inflate(R.layout.bottom_sheet_result, null)
        bottomSheetDialog.setContentView(view)

        // --- [닫기 기능 구현] ---
        // 닫기 버튼 누르면 다이얼로그 사라지기
        val closeBtn = view.findViewById<View>(R.id.btnClose)
        closeBtn.setOnClickListener { bottomSheetDialog.dismiss() }

        // 다시하기 버튼에도 기능 연결 가능
        val confirmBtn = view.findViewById<View>(R.id.btnRegenerateConfirm)
        val progressBar = view.findViewById<View>(R.id.loadingProgressBar)

        confirmBtn?.setOnClickListener {
            // 플리 다시 생성하는 코드
            // Toast.makeText(requireContext(), "죄송합니다.베타버전에서는\n지원하지 않는 기능입니다.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.VISIBLE
            confirmBtn.isEnabled = false

            regeneratePlaylist {
                bottomSheetDialog.dismiss()
                // binding.recyclerView.scrollToPosition(0)
                binding.appBarLayout.setExpanded(true, true)
            }
        }
        bottomSheetDialog.show()
    }

    // 플리 다시 생성하는 함수
    private fun regeneratePlaylist(onComplete: () -> Unit) {
        val request = RecommendationRequest(
            place = RecommendationManager.englishPlace,
            decibel = RecommendationManager.decibel,
            goal = RecommendationManager.englishGoal
        )
        viewModel.checkData()

        // 서버에서 응답 받아서 뷰모델에 저장
        RetrofitClient.recommendationApi.sendRecommendations(request).enqueue(object : Callback<BaseResponse<RecommendationResponse>> {

            override fun onResponse(
                call: Call<BaseResponse<RecommendationResponse>>,
                response: Response<BaseResponse<RecommendationResponse>>
            ) {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    val resultData = baseResponse?.data

                    if (resultData != null) {

                        RecommendationManager.cachedPlaylist = resultData
                        // 뷰모델 업데이트
                        viewModel.currentPlaylist.value = resultData

                        // 내부 저장소에 플리 이름 저장 -> result의 두 프래그먼트에서 동일한 이름 패치되도록
                        context?.let { ctx ->
                            RecommendationManager.savePlaylistName(ctx, resultData.playlistName)
                            RecommendationManager.savePlaylistId(ctx, resultData.playlistId.toString())  // 아이디 저장
                        }

                        // UI 업데이트 하기
                        updateUIWithRealData(resultData, RecommendationManager.place, RecommendationManager.goal)
                        onComplete()

                    } else {
                        Toast.makeText(context, "서버 응답이 비어있습니다.", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                } else {
                    Log.e(TAG, "서버 에러 코드: ${response.code()}")
                    Toast.makeText(context, "서버 에러 발생", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            }

            override fun onFailure(call: Call<BaseResponse<RecommendationResponse>>, t: Throwable) {
                Log.e(TAG, "통신 실패: ${t.message}")
                Toast.makeText(context, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        })
    }

    // 플리 이름 수정 코드
    private fun showAddLibraryBottomSheet(currentName: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
        bottomSheetDialog.setContentView(view)

        // 뷰 찾기
        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmEdit)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        // 현재 이름 입력창에 넣어두기
        etName.setText(currentName)
        // 커서를 글자 맨 뒤로 이동
        etName.setSelection(currentName.length)

        // 닫기(X) 버튼
        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 수정하기 버튼 눌렀을 때 이벤트 처리
        btnConfirm.setOnClickListener {
            // 위에서 이미 isEnabled로 막아뒀기 때문에, 여기서는 굳이 빈 값 체크를 안 해도 되지만 안전을 위해 유지
            val newName = etName.text.toString().trim()
            // 액티비티 화면의 제목을 변경
            binding.tvPlaylistName.text = newName
            // 안내 메시지 및 닫기
            val layout = layoutInflater.inflate(R.layout.toast_custom, null)
            val iconView = layout.findViewById<ImageView>(R.id.iv_toast_icon)
            iconView?.visibility = View.VISIBLE // 아이콘이 있으면 GONE 처리

            // 수정한 이름 내부 저장소에 저장
            context?.let { ctx ->
                RecommendationManager.savePlaylistName(ctx, newName)
            }
            // 저장한 id 백엔드로 보내기
            val savedPlaylistId = context?.let { ctx ->
                RecommendationManager.getPlaylistId(ctx)
            } ?: ""
            // *** 백엔드로 수정된 플리이름 보내는 함수 ***
            updatePlaylistNameOnServer(savedPlaylistId,newName)

            showCustomToast("내 라이브러리에 추가됐어요")
            bottomSheetDialog.dismiss()

            binding.btnAddLibrary.visibility = View.GONE
            binding.btnMoveToLibrary.visibility = View.VISIBLE
        }
        bottomSheetDialog.show()
    }

    // --- 백엔드에 이름 수정 요청 보내기 ---
    private fun updatePlaylistNameOnServer(playlistId: String, newName: String) {
        val requestBody = UpdatePlaylistNameRequest(newPlaylistName = newName)


        // API 호출
        RetrofitClient.recommendationApi.updatePlaylistName(playlistId, requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                } else {
                    // 실패 로그 (하지만 이미 화면은 바꿨으니 조용히 로그만 남김)
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "수정 실패 Code: ${response.code()}")

                    Toast.makeText(context, "서버 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e("API_UPDATE", "통신 에러: ${t.message}")
            }
        })
    }

    // --- 커스텀 토스트 사용하기 위한 함수 ---
    fun showCustomToast(message: String, iconResId: Int? = null) {
        // 1. 커스텀 레이아웃 불러오기 (Context 변경)
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.toast_custom, null)

        // 2. 텍스트 설정
        val textView = layout.findViewById<TextView>(R.id.tv_toast_message)
        textView.text = message

        // 4. 토스트 생성 및 설정 (applicationContext -> requireContext())
        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout // 내가 만든 레이아웃을 끼워넣음

        // 위치 조정 (선택사항: 화면 중앙 하단 등)
        toast.setGravity(Gravity.BOTTOM, 0, 300)

        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 뷰가 파괴될 때 바인딩 해제 (메모리 누수 방지)
        _binding = null
    }
}