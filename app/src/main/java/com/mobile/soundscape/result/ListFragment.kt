package com.mobile.soundscape.result

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
import com.mobile.soundscape.databinding.FragmentListBinding
import com.mobile.soundscape.recommendation.RecommendationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecommendationViewModel by activityViewModels()
    private lateinit var adapter: PlaylistResultAdapter

    // 폴링 제어용 Job
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
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
        adapter = PlaylistResultAdapter(songList) {
            // 푸터 클릭 시 바텀시트
            showRegenerateBottomSheet()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@ListFragment.adapter
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

    // --- 재생성(Regenerate) 바텀시트 ---
    private fun showRegenerateBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.bottom_sheet_result, null)

        bottomSheetDialog.setContentView(view)

        val closeBtn = view.findViewById<View>(R.id.btnClose)
        closeBtn.setOnClickListener { bottomSheetDialog.dismiss() }

        val confirmBtn = view.findViewById<View>(R.id.btnRegenerateConfirm)

        confirmBtn?.setOnClickListener {

            bottomSheetDialog.dismiss()  // 바텀 시트 닫기
            binding.loadingProgressBar.visibility = View.VISIBLE  // ProgressBar 표시

            // 폴링 시작
            startAsyncRegeneration()
        }
        bottomSheetDialog.show()
    }

    /**
     * [Step 1] 비동기 재생성 요청
     */
    private fun startAsyncRegeneration() {
        val request = RecommendationRequest(
            place = RecommendationManager.englishPlace,
            decibel = RecommendationManager.decibel,
            goal = RecommendationManager.englishGoal
        )

        RetrofitClient.recommendationApi.sendPlaylistPolling(request).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if (response.isSuccessful) {
                    val message = response.body()?.data
                    if (message != null) {
                        val regex = "taskId:\\s*([^\\s]+)".toRegex()
                        val matchResult = regex.find(message)
                        val taskId = matchResult?.groupValues?.get(1)

                        if (taskId != null) {
                            startPollingLoop(taskId)
                        } else {
                            handleError("작업 ID를 찾을 수 없습니다.")
                        }
                    } else {
                        handleError("서버 응답이 비어있습니다.")
                    }
                } else {
                    handleError("서버 요청 실패")
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                handleError("네트워크 오류가 발생했습니다.")
            }
        })
    }

    /**
     * [Step 2] 폴링 루프 (20초 타임아웃)
     */
    private fun startPollingLoop(taskId: String) {
        pollingJob?.cancel()

        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            val timeoutDuration = 20000L // 20초
            var isFinished = false

            while (isActive && !isFinished) {
                // 타임아웃 체크
                if (System.currentTimeMillis() - startTime > timeoutDuration) {
                    handleError("생성 시간이 초과되었습니다. 다시 시도해주세요.")
                    return@launch
                }

                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.recommendationApi.getPlaylistPolling(taskId).execute()
                    }

                    if (response.isSuccessful) {
                        val pollingData = response.body()?.data

                        if (pollingData != null) {
                            // 완료 확인
                            if (pollingData.status == "COMPLETED" && pollingData.playlistInfo != null) {
                                isFinished = true
                                onRegenerationComplete(pollingData.playlistInfo)
                            } else {
                                // 진행 중... 계속 대기
                            }
                        }
                    } else {
                        // 에러 코드가 오더라도 일단 재시도 (심각하면 중단 로직 추가 가능)
                    }

                } catch (e: Exception) {
                    // 네트워크 에러 등 예외 발생 시 무시하고 재시도
                }

                if (!isFinished) {
                    delay(2000) // 2초 대기
                }
            }
        }
    }

    /**
     * [Step 3] 재생성 완료 처리
     */
    private fun onRegenerationComplete(resultData: RecommendationResponse) {
        // UI 업데이트는 메인 스레드에서
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            // 1. 프로그래스바 숨기기
            binding.loadingProgressBar.visibility = View.GONE

            // 2. 데이터 캐싱 및 저장
            RecommendationManager.cachedPlaylist = resultData
            viewModel.currentPlaylist.value = resultData

            context?.let { ctx ->
                RecommendationManager.savePlaylistName(ctx, resultData.playlistName)
                RecommendationManager.savePlaylistId(ctx, resultData.playlistId.toString())
            }

            // 3. UI 갱신
            updateUIWithRealData(resultData, RecommendationManager.place, RecommendationManager.goal)

            // 4. 스크롤 맨 위로 이동
            binding.recyclerView.scrollToPosition(0)
            binding.appBarLayout.setExpanded(true, true)

            Toast.makeText(context, "플레이리스트가 새로고침 되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 에러 처리 공통 함수
    private fun handleError(msg: String) {
        // UI 작업은 메인 스레드에서
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            binding.loadingProgressBar.visibility = View.GONE
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // --- 이름 수정 바텀시트 ---
    private fun showAddLibraryBottomSheet(currentName: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
        bottomSheetDialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmEdit)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        etName.setText(currentName)
        etName.setSelection(currentName.length)

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val newName = etName.text.toString().trim()
            binding.tvPlaylistName.text = newName

            // 커스텀 토스트 (아이콘 포함)
            val layout = layoutInflater.inflate(R.layout.toast_custom, null)
            val iconView = layout.findViewById<ImageView>(R.id.iv_toast_icon)
            iconView?.visibility = View.VISIBLE

            context?.let { ctx ->
                RecommendationManager.savePlaylistName(ctx, newName)
            }
            val savedPlaylistId = context?.let { ctx ->
                RecommendationManager.getPlaylistId(ctx)
            } ?: ""

            updatePlaylistNameOnServer(savedPlaylistId, newName)

            showCustomToast("내 라이브러리에 추가됐어요")
            bottomSheetDialog.dismiss()

            binding.btnAddLibrary.visibility = View.GONE
            binding.btnMoveToLibrary.visibility = View.VISIBLE
        }
        bottomSheetDialog.show()
    }

    private fun updatePlaylistNameOnServer(playlistId: String, newName: String) {
        val requestBody = UpdatePlaylistNameRequest(newPlaylistName = newName)

        RetrofitClient.recommendationApi.updatePlaylistName(playlistId, requestBody).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(call: Call<BaseResponse<String>>, response: Response<BaseResponse<String>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(context, "서버 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                // 로그 제거됨
            }
        })
    }

    fun showCustomToast(message: String, iconResId: Int? = null) {
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
        pollingJob?.cancel() // 폴링 중단
        _binding = null
    }
}