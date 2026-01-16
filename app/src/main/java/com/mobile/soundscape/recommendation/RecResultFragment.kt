package com.mobile.soundscape.recommendation

import android.R.id.message
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mobile.soundscape.R
import com.mobile.soundscape.api.client.RetrofitClient
import com.mobile.soundscape.api.dto.BaseResponse
import com.mobile.soundscape.api.dto.RecommendationRequest
import com.mobile.soundscape.api.dto.RecommendationResponse
import com.mobile.soundscape.data.AppDatabase
import com.mobile.soundscape.data.PlaylistHistory
import com.mobile.soundscape.data.RecommendationManager
import com.mobile.soundscape.databinding.FragmentRecResultBinding
import com.mobile.soundscape.result.PlaylistResultActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.getValue
import kotlin.io.path.Path

class RecResultFragment : Fragment() {

    private var _binding: FragmentRecResultBinding? = null
    private val binding get() = _binding!!
    val TAG = "RecResultFragment"
    private val viewModel: RecommendationViewModel by activityViewModels()

    private var isDataLoaded = false
    private var pollingJob: Job? = null // 폴링 작업 취소하기 위한 변순

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 구슬 움직이는 animation
        com.bumptech.glide.Glide.with(this)
            .load(R.drawable.orb_animation)
            .into(binding.centerButton)

        // 비동기 작업 시작 요청 -> 장소, 데시벨, 목표를 백엔드로 전송
        startAsyncGeneration()

        // "보러가기" 버튼을 누르면 -> 플레이리스트로 이동
        binding.nextButton.setOnClickListener {
            if (isDataLoaded) {
                findNavController().navigate(R.id.action_recResultFragment_to_listFragment)
            } else {
                // 아직 데이터 저장 중이면 조금만 기다려달라고 하기
                Toast.makeText(requireContext(), "플레이리스트를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * [Step 1]
     * 1. (polling 방식) 장소, 데시벨, 목표를 백엔드로 전송
     * 2. 비동기 작업 요청
     * 3. taskId 받아오기
     */
    private fun startAsyncGeneration() {
        // 뷰모델에서 장소, 데시벨, 목표 꺼내기
        val request = RecommendationRequest(
            place = viewModel.englishPlace,
            decibel = viewModel.decibel,
            goal = viewModel.englishGoal
        )
        viewModel.checkData()

        RetrofitClient.recommendationApi.sendPlaylistPolling(request).enqueue(object : Callback<BaseResponse<String>> {
            override fun onResponse(
                call: Call<BaseResponse<String>>,
                response: Response<BaseResponse<String>>
            ) {
                if(response.isSuccessful) {
                    val message = response.body()?.data
                    // ex: "data": "플레이리스트 생성 작업이 시작되었습니다. taskId: task_ZND0fTVRk9QOb9g"

                    // 문자열에서 taskId 추출
                    if(message != null) {
                        val regex = "taskId:\\s*([^\\s]+)".toRegex()
                        val matchResult = regex.find(message)

                        val taskId = matchResult?.groupValues?.get(1)

                        // 4. taskId가 잘 구해졌으면 폴링 시작
                        if (taskId != null) {
                            Log.d(TAG, "taskId 찾음: $taskId")
                            startPollingLoop(taskId)
                        }

                    } else {
                        Log.e(TAG, "Task ID 파싱 실패: $message")
                        Toast.makeText(context, "작업 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "작업 요청 실패: ${response.code()}")
                    Toast.makeText(context, "서버 요청 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse<String>>, t: Throwable) {
                Log.e(TAG, "네트워크 오류: ${t.message}")
                Toast.makeText(context, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * [Step 2] 2초 간격으로 상태 확인 (Polling)
     */
    private fun startPollingLoop(taskId: String) {
        // 기존 작업이 있다면 취소
        pollingJob?.cancel()

        // 프래그먼트 생명주기에 맞춰 코루틴 실행
        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            var isFinished = false

            while (isActive && !isFinished) { // 화면이 살아있는 동안 반복
                try {
                    // API 호출 - 네트워크 요청을 메인 스레드로 보내면  android.os.NetworkOnMainThreadException 오류냄
                    // IO 스레드(백그라운드)로 보내야 오류 안남 
                     val response = kotlinx.coroutines.withContext(Dispatchers.IO) {
                         RetrofitClient.recommendationApi.getPlaylistPolling(taskId).execute()
                     }

                    if (response.isSuccessful) {
                        val pollingData = response.body()?.data

                        if (pollingData != null) {
                            // 성공 여부 판단 로직 - playlistInfo가 null이 아니면 완료된 것으로 간주
                            if (pollingData.status == "COMPLETED" && pollingData.playlistInfo != null) {
                                Log.d(TAG, "작업 완료! 결과 수신 성공")
                                isFinished = true
                                // 3. 결과 처리
                                onPlaylistReady(pollingData.playlistInfo)
                            } else {
                                // "IN_PROGRESS" 이거나 다른 상태일 때
                                Log.d(TAG, "작업 진행 중... (Status: ${pollingData.status})")
                            }
                        }
                    } else {
                        Log.e(TAG, "폴링 상태 확인 실패: ${response.code()}")
                        // 404나 500 등 치명적 에러면 루프 종료 여부 결정 필요 (일단은 계속 시도하거나 종료)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "폴링 중 에러: ${e.message}")
                }

                if (!isFinished) {
                    // 2초 대기 후 다시 루프
                    delay(2000)
                }
            }
        }
    }

    /**
     * [Step 3] 최종 결과 처리 (기존 로직 이동)
     */
    private fun onPlaylistReady(resultData: RecommendationResponse) {
        isDataLoaded = true

        // 메모리(싱글톤)에 저장
        RecommendationManager.englishGoal = viewModel.englishGoal
        RecommendationManager.englishPlace = viewModel.englishPlace
        RecommendationManager.decibel = viewModel.decibel
        RecommendationManager.place = viewModel.place
        RecommendationManager.goal = viewModel.goal
        RecommendationManager.cachedPlaylist = resultData

        // 뷰모델 업데이트
        viewModel.currentPlaylist.value = resultData

        // 내부 저장소에 플리 이름 저장 -> result의 두 프래그먼트에서 동일한 이름 패치되도록
        context?.let { ctx ->
            com.mobile.soundscape.PreferenceManager.setPlaylistExperienced(ctx, true)

            RecommendationManager.savePlaylistName(ctx, resultData.playlistName)
            RecommendationManager.savePlaylistId(ctx, resultData.playlistId.toString())

            // Room Database 에 최근 기록 저장
            saveToRoomHistory(ctx, viewModel.place, viewModel.goal, resultData.playlistId)
        }

        // UI 업데이트 (메인 스레드에서 실행 보장)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            updateUIForCompletion()
        }

    }


    private fun updateUIForCompletion() {
        // 뷰 바인딩이 유효한지 확인 (혹시 모를 크래시 방지)
        if (_binding == null) return

        binding.apply {
            // 1. 텍스트 변경
            tvSubtitle.text = "오늘의 몰입 플레이리스트가\n완성됐어요"

            // 2. "잠시만 기다려주세요" 텍스트 숨기기
            tvSubtitle2.visibility = View.GONE

            // 3. 버튼과 타원(ellipse) 보이기
            nextButton.visibility = View.VISIBLE
            ellipse.visibility = View.VISIBLE

            // 보러가기 버튼 애니메이션 주기
            nextButton.alpha = 0f
            nextButton.animate().alpha(1f).setDuration(500).start()

            ellipse.alpha = 0f
            ellipse.animate().alpha(1f).setDuration(1000).start()
        }
    }

    private fun saveToRoomHistory(context: Context, place: String, goal: String, playlistId: Int) {
        val history = PlaylistHistory(
            place = place,
            goal = translateGoal(goal),
            iconResName = getIconNameForPlace(place),
            playlistId = playlistId,
            timestamp = System.currentTimeMillis()
        )

        // GlobalScope나 별도 Scope 대신 viewLifecycleOwner.lifecycleScope 사용 권장
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                AppDatabase.getDatabase(context).historyDao().insert(history)

                Log.d("RoomDB_CHECK", "저장 성공! ID: $playlistId, Place: $place")
            } catch (e: Exception) {
                Log.e("RoomDB_CHECK", "저장 실패: ${e.message}")
            }
        }
    }
    //영문 -> 한글로
    private fun translateGoal(goal: String?): String {
        return when (goal?.lowercase()) {
            "sleep" -> "수면"
            "focus" -> "집중"
            "cosolation" -> "위로"
            "active" -> "활력"
            "stabilization" -> "안정"
            "anger" -> "분노"
            "relax" -> "휴식"
            "neutral", null -> "미선택"
            else -> goal ?: "미선택"
        }
    }

    //홈에 뜨게 할 아이콘
    private fun getIconNameForPlace(place: String): String {
        return when (place) {
            "집/실내" -> "place_icon0"
            "카페" -> "place_icon1"
            "코워킹" -> "place_icon2"
            "헬스장" -> "place_icon3"
            "도서관" -> "place_icon4"
            "이동중" -> "place_icon5"
            "공원" -> "place_icon6"
            else -> "place_icon0"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}